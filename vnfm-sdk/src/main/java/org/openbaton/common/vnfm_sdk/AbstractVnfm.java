/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.common.vnfm_sdk;

import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.*;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleChangeNotification;
import org.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleManagement;
import org.openbaton.common.vnfm_sdk.utils.VNFRUtils;
import org.openbaton.common.vnfm_sdk.utils.VnfmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by lto on 08/07/15.
 */
public abstract class AbstractVnfm implements VNFLifecycleManagement, VNFLifecycleChangeNotification {

    protected static String type;
    protected static String endpoint;
    protected static String endpointType;
    protected static Properties properties;
    protected static Logger log = LoggerFactory.getLogger(AbstractVnfm.class);
    protected VnfmHelper vnfmHelper;
    protected VnfmManagerEndpoint vnfmManagerEndpoint;
    private ExecutorService executor;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    @PreDestroy
    private void shutdown() {
    }

    @PostConstruct
    private void init() {
        setup();
        executor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("concurrency", "15")));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public abstract void query();

    @Override
    public abstract VirtualNetworkFunctionRecord scale(Action scaleInOrOut, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance component, Object scripts, VNFRecordDependency dependency) throws Exception;

    @Override
    public abstract void checkInstantiationFeasibility();

    @Override
    public abstract VirtualNetworkFunctionRecord heal(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance component, String cause) throws Exception;

    @Override
    public abstract void updateSoftware();

    @Override
    public abstract VirtualNetworkFunctionRecord modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFRecordDependency dependency) throws Exception;

    @Override
    public abstract void upgradeSoftware();

    @Override
    public abstract VirtualNetworkFunctionRecord terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

    public abstract void handleError(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    protected void loadProperties() {
        properties = new Properties();
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("conf.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        endpoint = (String) properties.get("endpoint");
        type = (String) properties.get("type");
        endpointType = properties.getProperty("endpoint-type", "RABBIT");
        description = properties.getProperty("description", "");
    }

    protected void onAction(NFVMessage message) throws NotFoundException, BadFormatException {

        String nsrId = "";
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = null;
        try {

            log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + message.getAction() + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            log.trace("VNFM: Received Message: " + message.getAction());
            NFVMessage nfvMessage = null;
            OrVnfmGenericMessage orVnfmGenericMessage = null;
            switch (message.getAction()) {
                case SCALE_IN:
                    OrVnfmScalingMessage scalingMessage = (OrVnfmScalingMessage) message;
                    nsrId = scalingMessage.getVirtualNetworkFunctionRecord().getParent_ns_id();
                    virtualNetworkFunctionRecord = scalingMessage.getVirtualNetworkFunctionRecord();
                    VNFCInstance vnfcInstanceToRemove = scalingMessage.getVnfcInstance();

                    virtualNetworkFunctionRecord = this.scale(Action.SCALE_IN, virtualNetworkFunctionRecord, vnfcInstanceToRemove, null, null);
                    nfvMessage = null;
                    break;
                case SCALE_OUT:

                    scalingMessage = (OrVnfmScalingMessage) message;

                    nsrId = scalingMessage.getVirtualNetworkFunctionRecord().getParent_ns_id();
                    virtualNetworkFunctionRecord = scalingMessage.getVirtualNetworkFunctionRecord();
                    VNFRecordDependency dependency = scalingMessage.getDependency();
                    VNFComponent component = scalingMessage.getComponent();
                    String mode = scalingMessage.getMode();

                    log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
                    log.info("Adding VNFComponent: " + component);
                    log.debug("the mode is:" + mode);


                    if (!properties.getProperty("allocate", "true").equalsIgnoreCase("true")) {

                        NFVMessage message2 = vnfmHelper.sendAndReceive(VnfmUtils.getNfvMessage(Action.SCALING, virtualNetworkFunctionRecord));
                        if (message2 instanceof OrVnfmGenericMessage) {
                            OrVnfmGenericMessage message1 = (OrVnfmGenericMessage) message2;
                            virtualNetworkFunctionRecord = message1.getVnfr();
                            log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
                        } else if (message2 instanceof OrVnfmErrorMessage) {
                            this.handleError(((OrVnfmErrorMessage) message2).getVnfr());
                            return;
                        }
                    }

                    boolean found = false;
                    VNFCInstance vnfcInstance_new = null;
                    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                        for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                            if (vnfcInstance.getVnfComponent().getId().equals(component.getId())) {
                                vnfcInstance_new = vnfcInstance;
                                fillProvidesVNFC(virtualNetworkFunctionRecord, vnfcInstance);
                                found = true;
                                log.debug("VNFComponentInstance FOUND : " + vnfcInstance_new.getVnfComponent());
                                break;
                            }
                        }
                        if (found)
                            break;
                    }
                    if (vnfcInstance_new == null) {
                        throw new RuntimeException("no new VNFCInstance found. This should not happen...");
                    }
                    if(mode!=null && mode.equals("standby"))
                        vnfcInstance_new.setState(mode);

                    checkEMS(vnfcInstance_new.getHostname());
                    Object scripts;
                    if (scalingMessage.getVnfPackage().getScriptsLink() != null)
                        scripts = scalingMessage.getVnfPackage().getScriptsLink();
                    else
                        scripts = scalingMessage.getVnfPackage().getScripts();
                    nfvMessage = VnfmUtils.getNfvMessageScaled(Action.SCALED, this.scale(Action.SCALE_OUT, virtualNetworkFunctionRecord, vnfcInstance_new, scripts, dependency), vnfcInstance_new);
                    break;
                case SCALING:
                    break;
                case ERROR:
                    OrVnfmErrorMessage errorMessage = (OrVnfmErrorMessage) message;
                    nsrId = errorMessage.getVnfr().getParent_ns_id();
                    log.error("ERROR Received: " + errorMessage.getMessage());
                    handleError(errorMessage.getVnfr());

                    nfvMessage = null;
                    break;
                case MODIFY:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
                    virtualNetworkFunctionRecord = orVnfmGenericMessage.getVnfr();
                    nsrId = orVnfmGenericMessage.getVnfr().getParent_ns_id();
                    nfvMessage = VnfmUtils.getNfvMessage(Action.MODIFY, this.modify(orVnfmGenericMessage.getVnfr(), orVnfmGenericMessage.getVnfrd()));
                    break;
                case RELEASE_RESOURCES:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
                    nsrId = orVnfmGenericMessage.getVnfr().getParent_ns_id();
                    nfvMessage = VnfmUtils.getNfvMessage(Action.RELEASE_RESOURCES, this.terminate(orVnfmGenericMessage.getVnfr()));
                    break;
                case INSTANTIATE:
                    OrVnfmInstantiateMessage orVnfmInstantiateMessage = (OrVnfmInstantiateMessage) message;
                    Map<String, String> extension = orVnfmInstantiateMessage.getExtension();
                    nsrId = extension.get("nsr-id");
                    virtualNetworkFunctionRecord = createVirtualNetworkFunctionRecord(orVnfmInstantiateMessage.getVnfd(), orVnfmInstantiateMessage.getVnfdf().getFlavour_key(), orVnfmInstantiateMessage.getVlrs(), orVnfmInstantiateMessage.getExtension(), orVnfmInstantiateMessage.getVimInstances());
                    GrantOperation grantOperation = new GrantOperation();
                    grantOperation.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);

                    Future<OrVnfmGrantLifecycleOperationMessage> result = executor.submit(grantOperation);
                    OrVnfmGrantLifecycleOperationMessage msg =result.get();

                    virtualNetworkFunctionRecord = msg.getVirtualNetworkFunctionRecord();
                    Map<String, VimInstance> vimInstanceChosen = msg.getVduVim();

                            log.trace("VERISON IS: " + virtualNetworkFunctionRecord.getHb_version());

                    if (!properties.getProperty("allocate", "true").equalsIgnoreCase("true")) {
                        AllocateResources allocateResources = new AllocateResources();
                        allocateResources.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
                        allocateResources.setVimInstances(vimInstanceChosen);
                        virtualNetworkFunctionRecord = executor.submit(allocateResources).get();
                    }
                    setupProvides(virtualNetworkFunctionRecord);

                    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu())
                        for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance())
                            checkEMS(vnfcInstance.getHostname());
                    if (orVnfmInstantiateMessage.getVnfPackage() != null) {
                        if (orVnfmInstantiateMessage.getVnfPackage().getScriptsLink() != null)
                            virtualNetworkFunctionRecord = instantiate(virtualNetworkFunctionRecord, orVnfmInstantiateMessage.getVnfPackage().getScriptsLink(), orVnfmInstantiateMessage.getVimInstances());
                        else
                            virtualNetworkFunctionRecord = instantiate(virtualNetworkFunctionRecord, orVnfmInstantiateMessage.getVnfPackage().getScripts(), orVnfmInstantiateMessage.getVimInstances());
                    } else {
                        virtualNetworkFunctionRecord = instantiate(virtualNetworkFunctionRecord, null, orVnfmInstantiateMessage.getVimInstances());
                    }
                    nfvMessage = VnfmUtils.getNfvMessage(Action.INSTANTIATE, virtualNetworkFunctionRecord);
                    break;
                case RELEASE_RESOURCES_FINISH:
                    break;
                case HEAL:
                    OrVnfmHealVNFRequestMessage orVnfmHealMessage = (OrVnfmHealVNFRequestMessage) message;

                    nsrId = orVnfmHealMessage.getVirtualNetworkFunctionRecord().getParent_ns_id();
                    VirtualNetworkFunctionRecord vnfrObtained = this.heal(orVnfmHealMessage.getVirtualNetworkFunctionRecord(),orVnfmHealMessage.getVnfcInstance(),orVnfmHealMessage.getCause());
                    nfvMessage = VnfmUtils.getNfvMessageHealed(Action.HEAL, vnfrObtained,orVnfmHealMessage.getVnfcInstance());

                    break;
                case INSTANTIATE_FINISH:
                    break;
                case CONFIGURE:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
                    virtualNetworkFunctionRecord = orVnfmGenericMessage.getVnfr();
                    nsrId = orVnfmGenericMessage.getVnfr().getParent_ns_id();
                    nfvMessage = VnfmUtils.getNfvMessage(Action.CONFIGURE, configure(orVnfmGenericMessage.getVnfr()));
                    break;
                case START:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
                    virtualNetworkFunctionRecord = orVnfmGenericMessage.getVnfr();
                    nsrId = orVnfmGenericMessage.getVnfr().getParent_ns_id();
                    nfvMessage = VnfmUtils.getNfvMessage(Action.START, start(orVnfmGenericMessage.getVnfr()));
                    break;
            }

            log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            if (nfvMessage != null) {
                log.debug("send to NFVO");
                vnfmHelper.sendToNfvo(nfvMessage);
            }
        } catch (Exception e) {
            log.error("ERROR: ", e);
            if (e instanceof VnfmSdkException) {
                VnfmSdkException vnfmSdkException = (VnfmSdkException) e;
                if (vnfmSdkException.getVnfr() != null) {
                    log.debug("sending VNFR with version: " + vnfmSdkException.getVnfr().getHb_version());
                    vnfmHelper.sendToNfvo(VnfmUtils.getNfvErrorMessage(vnfmSdkException.getVnfr(), vnfmSdkException, nsrId));
                    return;
                }
            } else if (e.getCause() instanceof VnfmSdkException) {
                VnfmSdkException vnfmSdkException = (VnfmSdkException) e.getCause();
                if (vnfmSdkException.getVnfr() != null) {
                    log.debug("sending VNFR with version: " + vnfmSdkException.getVnfr().getHb_version());
                    vnfmHelper.sendToNfvo(VnfmUtils.getNfvErrorMessage(vnfmSdkException.getVnfr(), vnfmSdkException, nsrId));
                    return;
                }
            }
            vnfmHelper.sendToNfvo(VnfmUtils.getNfvErrorMessage(virtualNetworkFunctionRecord, e, nsrId));
        }
    }

    private void checkEMS(String vduHostname) {
        int i = 0;
        while (true) {
            log.debug("Waiting for ems to be started... (" + i * 5 + " secs)");
            i++;
            try {
                checkEmsStarted(vduHostname);
                break;
            } catch (RuntimeException e) {
                if (i == 100) {
                    throw e;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    protected abstract void checkEmsStarted(String vduHostname);

    private void setupProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    }

    private void fillProvidesVNFC(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance) {
    }

    /**
     * This method needs to set all the parameter specified in the VNFDependency.parameters
     *
     * @param virtualNetworkFunctionRecord
     */
    protected void fillSpecificProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    }

    /**
     * This method can be overwritten in case you want a specific initialization of the VirtualNetworkFunctionRecordShort from the VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor
     * @param extension
     * @param vimInstances
     * @return The new VirtualNetworkFunctionRecordShort
     * @throws BadFormatException
     * @throws NotFoundException
     */
    protected VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String flavourId, Set<VirtualLinkRecord> virtualLinkRecords, Map<String, String> extension, Map<String, Collection<VimInstance>> vimInstances) throws BadFormatException, NotFoundException {
        try {
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = VNFRUtils.createVirtualNetworkFunctionRecord(virtualNetworkFunctionDescriptor, flavourId, extension.get("nsr-id"), virtualLinkRecords, vimInstances);
            for (InternalVirtualLink internalVirtualLink : virtualNetworkFunctionRecord.getVirtual_link()) {
                for (VirtualLinkRecord virtualLinkRecord : virtualLinkRecords) {
                    if (internalVirtualLink.getName().equals(virtualLinkRecord.getName())) {
                        internalVirtualLink.setExtId(virtualLinkRecord.getExtId());
                        internalVirtualLink.setConnectivity_type(virtualLinkRecord.getConnectivity_type());
                    }
                }
            }
            log.debug("Created VirtualNetworkFunctionRecordShort: " + virtualNetworkFunctionRecord);
            return virtualNetworkFunctionRecord;
        } catch (NotFoundException e) {
            e.printStackTrace();
            vnfmHelper.sendToNfvo(VnfmUtils.getNfvMessage(Action.ERROR, null));
            throw e;
        } catch (BadFormatException e) {
            e.printStackTrace();
            vnfmHelper.sendToNfvo(VnfmUtils.getNfvMessage(Action.ERROR, null));
            throw e;
        }
    }

    public abstract VirtualNetworkFunctionRecord start(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

    public abstract VirtualNetworkFunctionRecord configure(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

    /**
     * This method unsubscribe the VNFM in the NFVO
     */
    protected abstract void unregister();

    /**
     * This method subscribe the VNFM to the NFVO sending the right endpoint
     */
    protected abstract void register();

    /**
     * This method setups the VNFM and then subscribe it to the NFVO. We recommend to not change this method or at least
     * to override calling super()
     */
    protected void setup() {
        loadProperties();
        vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setDescription(this.description);
        vnfmManagerEndpoint.setEnabled(true);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        log.debug("creating VnfmManagerEndpoint for vnfm endpointType: " + this.endpointType);
        vnfmManagerEndpoint.setEndpointType(EndpointType.valueOf(this.endpointType));
        register();
    }

    class GrantOperation implements Callable<OrVnfmGrantLifecycleOperationMessage> {
        private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

        public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
            return virtualNetworkFunctionRecord;
        }

        public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
            this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
        }

        private OrVnfmGrantLifecycleOperationMessage grantLifecycleOperation() throws VnfmSdkException {
            NFVMessage response;
            try {
                response = vnfmHelper.sendAndReceive(VnfmUtils.getNfvMessage(Action.GRANT_OPERATION, virtualNetworkFunctionRecord));
            } catch (Exception e) {
                throw new VnfmSdkException("Not able to grant operation", e, virtualNetworkFunctionRecord);
            }
            if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
                throw new VnfmSdkException("Not able to grant operation because: " + ((OrVnfmErrorMessage) response).getMessage(), ((OrVnfmErrorMessage) response).getVnfr());
            }
            OrVnfmGrantLifecycleOperationMessage orVnfmGrantLifecycleOperationMessage = (OrVnfmGrantLifecycleOperationMessage) response;
            return orVnfmGrantLifecycleOperationMessage;
        }

        @Override
        public OrVnfmGrantLifecycleOperationMessage call() throws Exception {
            return this.grantLifecycleOperation();
        }
    }

    class AllocateResources implements Callable<VirtualNetworkFunctionRecord> {
        private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

        public void setVimInstances(Map<String, VimInstance> vimInstances) {
            this.vimInstances = vimInstances;
        }

        private Map<String, VimInstance> vimInstances;

        public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
            return virtualNetworkFunctionRecord;
        }

        public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
            this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
        }


        public VirtualNetworkFunctionRecord allocateResources() throws VnfmSdkException {
            NFVMessage response;
            try {


                response = vnfmHelper.sendAndReceive(VnfmUtils.getNfvInstantiateMessage(virtualNetworkFunctionRecord, vimInstances));
            } catch (Exception e) {
                log.error("" + e.getMessage());
                throw new VnfmSdkException("Not able to allocate Resources", e, virtualNetworkFunctionRecord);
            }
            if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
                OrVnfmErrorMessage errorMessage = (OrVnfmErrorMessage) response;
                log.error(errorMessage.getMessage());
                virtualNetworkFunctionRecord = errorMessage.getVnfr();
                throw new VnfmSdkException("Not able to allocate Resources because: " + errorMessage.getMessage(), virtualNetworkFunctionRecord);
            }
            OrVnfmGenericMessage orVnfmGenericMessage = (OrVnfmGenericMessage) response;
            log.debug("Received from ALLOCATE: " + orVnfmGenericMessage.getVnfr());
            return orVnfmGenericMessage.getVnfr();
        }

        @Override
        public VirtualNetworkFunctionRecord call() throws Exception {
            return this.allocateResources();
        }

    }
}
