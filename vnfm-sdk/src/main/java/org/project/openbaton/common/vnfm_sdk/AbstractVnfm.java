package org.project.openbaton.common.vnfm_sdk;

import org.project.openbaton.catalogue.mano.common.Ip;
import org.project.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.project.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.project.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.project.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleChangeNotification;
import org.project.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleManagement;
import org.project.openbaton.common.vnfm_sdk.utils.VNFRUtils;
import org.project.openbaton.common.vnfm_sdk.utils.VnfmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by lto on 08/07/15.
 */
public abstract class AbstractVnfm implements VNFLifecycleManagement, VNFLifecycleChangeNotification {

    protected VnfmHelper vnfmHelper;
    protected String type;
    protected String endpoint;
    protected String endpointType;
    protected Properties properties;
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected VnfmManagerEndpoint vnfmManagerEndpoint;
//    protected VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    @PreDestroy
    private void shutdown() {
        this.unregister();
    }

    @PostConstruct
    private void init() {
        setup();
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
    public abstract void scale();

    @Override
    public abstract void checkInstantiationFeasibility();

    @Override
    public abstract void heal();

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
        Resource resource = new ClassPathResource("conf.properties");
        properties = new Properties();
        try {
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        this.endpoint = (String) properties.get("endpoint");
        this.type = (String) properties.get("type");
        this.endpointType = properties.getProperty("endpoint-type", "JMS");
    }

    protected void onAction(NFVMessage message) throws NotFoundException, BadFormatException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = null;
        try {

            log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + message.getAction() + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            log.trace("VNFM: Received Message: " + message.getAction());
            NFVMessage nfvMessage = null;
            OrVnfmGenericMessage orVnfmGenericMessage = null;
            switch (message.getAction()) {
                case SCALE:
                    this.scale();
                    break;
                case SCALING:
                    break;
                case ERROR:
                    OrVnfmErrorMessage errorMessage = (OrVnfmErrorMessage) message;
                    log.error("ERROR Received: " + errorMessage.getMessage());
                    handleError(errorMessage.getVnfr());

                    nfvMessage = null;
                    break;
                case MODIFY:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
                    nfvMessage = VnfmUtils.getNfvMessage(Action.MODIFY, this.modify(orVnfmGenericMessage.getVnfr(), orVnfmGenericMessage.getVnfrd()));
                    break;
                case RELEASE_RESOURCES:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
                    nfvMessage = VnfmUtils.getNfvMessage(Action.RELEASE_RESOURCES, this.terminate(orVnfmGenericMessage.getVnfr()));
                    break;
                case INSTANTIATE:
                    OrVnfmInstantiateMessage orVnfmInstantiateMessage = (OrVnfmInstantiateMessage) message;
                    virtualNetworkFunctionRecord = createVirtualNetworkFunctionRecord(orVnfmInstantiateMessage.getVnfd(), orVnfmInstantiateMessage.getVnfdf().getFlavour_key(), orVnfmInstantiateMessage.getVnfd().getName(), orVnfmInstantiateMessage.getVlrs(), orVnfmInstantiateMessage.getExtention());
                    virtualNetworkFunctionRecord = vnfmHelper.grantLifecycleOperation(virtualNetworkFunctionRecord).get();
                    if (properties.getProperty("allocate", "false").equalsIgnoreCase("true"))
                        virtualNetworkFunctionRecord = vnfmHelper.allocateResources(virtualNetworkFunctionRecord).get();
                    setupProvides(virtualNetworkFunctionRecord);

                    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu())
                        for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance())
                            checkEMS(vnfcInstance.getHostname());

                    if (orVnfmInstantiateMessage.getScriptsLink() != null)
                        virtualNetworkFunctionRecord = instantiate(virtualNetworkFunctionRecord, orVnfmInstantiateMessage.getScriptsLink());
                    else
                        virtualNetworkFunctionRecord = instantiate(virtualNetworkFunctionRecord, orVnfmInstantiateMessage.getScripts());
                    nfvMessage = VnfmUtils.getNfvMessage(Action.INSTANTIATE, virtualNetworkFunctionRecord);
                    break;
                case RELEASE_RESOURCES_FINISH:
                    break;
                case INSTANTIATE_FINISH:
                    break;
                case CONFIGURE:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
                    nfvMessage = VnfmUtils.getNfvMessage(Action.CONFIGURE, configure(orVnfmGenericMessage.getVnfr()));
                    break;
                case START:
                    orVnfmGenericMessage = (OrVnfmGenericMessage) message;
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
            if (e instanceof VnfmSdkException){
                VnfmSdkException vnfmSdkException = (VnfmSdkException) e;
                if (vnfmSdkException.getVnfr() != null){
                    log.debug("sending vnfr with version: " + vnfmSdkException.getVnfr().getHb_version());
                    vnfmHelper.sendToNfvo(VnfmUtils.getNfvMessage(Action.ERROR, vnfmSdkException.getVnfr()));
                    return;
                }
            }
            vnfmHelper.sendToNfvo(VnfmUtils.getNfvMessage(Action.ERROR, virtualNetworkFunctionRecord));
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
        fillSpecificProvides(virtualNetworkFunctionRecord);

        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
            for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                int i = 1;
                for (Ip ip : vnfcInstance.getIps()) {
                    ConfigurationParameter cp = new ConfigurationParameter();
                    cp.setConfKey(ip.getNetName() + i);
                    cp.setValue(ip.getIp());
                    virtualNetworkFunctionRecord.getProvides().getConfigurationParameters().add(cp);
                    i++;
                }
            }
        }
        log.debug("Provides is: " + virtualNetworkFunctionRecord.getProvides());
    }

    /**
     * This method needs to set all the parameter specified in the VNFDependency.parameters
     *
     * @param virtualNetworkFunctionRecord
     */
    protected void fillSpecificProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    }

    /**
     * This method can be overwritten in case you want a specific initialization of the VirtualNetworkFunctionRecord from the VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor
     * @param extension
     * @return The new VirtualNetworkFunctionRecord
     * @throws BadFormatException
     * @throws NotFoundException
     */
    protected VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String flavourId, String vnfInstanceName, Set<VirtualLinkRecord> virtualLinkRecords, Map<String, String> extension) throws BadFormatException, NotFoundException {
        try {
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = VNFRUtils.createVirtualNetworkFunctionRecord(virtualNetworkFunctionDescriptor, flavourId, extension.get("nsr-id"), virtualLinkRecords);
            for (InternalVirtualLink internalVirtualLink : virtualNetworkFunctionRecord.getVirtual_link()) {
                for (VirtualLinkRecord virtualLinkRecord : virtualLinkRecords) {
                    if (internalVirtualLink.getName().equals(virtualLinkRecord.getName())) {
                        internalVirtualLink.setExtId(virtualLinkRecord.getExtId());
                        internalVirtualLink.setConnectivity_type(virtualLinkRecord.getConnectivity_type());
                    }
                }
            }
            log.debug("Created VirtualNetworkFunctionRecord: " + virtualNetworkFunctionRecord);
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
     * This method unregister the VNFM in the NFVO
     */
    protected abstract void unregister();

    /**
     * This method register the VNFM to the NFVO sending the right endpoint
     */
    protected abstract void register();

    /**
     * This method setups the VNFM and then register it to the NFVO. We recommend to not change this method or at least
     * to override calling super()
     */
    protected void setup() {
        loadProperties();
        vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        log.debug("creating VnfmManagerEndpoint for vnfm endpointType: " + this.endpointType);
        vnfmManagerEndpoint.setEndpointType(EndpointType.valueOf(this.endpointType));
        register();
    }

    protected Map<String, String> getMap(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        Map<String, String> res = new HashMap<>();
        for (ConfigurationParameter configurationParameter : virtualNetworkFunctionRecord.getProvides().getConfigurationParameters())
            res.put(configurationParameter.getConfKey(),configurationParameter.getValue());
        for (ConfigurationParameter configurationParameter : virtualNetworkFunctionRecord.getConfigurations().getConfigurationParameters()){
            res.put(configurationParameter.getConfKey(),configurationParameter.getValue());
        }
        return res;
    }
}
