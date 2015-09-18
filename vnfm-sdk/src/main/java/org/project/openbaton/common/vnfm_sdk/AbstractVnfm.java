package org.project.openbaton.common.vnfm_sdk;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.project.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.project.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.project.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.project.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.project.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleChangeNotification;
import org.project.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleManagement;
import org.project.openbaton.common.vnfm_sdk.utils.VNFRUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

/**
 * Created by lto on 08/07/15.
 */
public abstract class AbstractVnfm implements VNFLifecycleManagement, VNFLifecycleChangeNotification {
    protected static final String nfvoQueue = "vnfm-core-actions";
    protected String type;
    protected String endpoint;
    protected String endpointType;
    protected Set<VirtualLinkRecord> vlr;
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

    public Set<VirtualLinkRecord> getVlr() {
        return vlr;
    }

    public void setVlr(Set<VirtualLinkRecord> vlr) {
        this.vlr = vlr;
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
    public abstract VirtualNetworkFunctionRecord terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    public abstract CoreMessage handleError(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

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
            OrVnfmGenericMessage orVnfmGenericMessage=null;
            switch (message.getAction()) {
                case SCALE:
                    this.scale();
                    break;
                case SCALING:
                    break;
                case ERROR:
                    orVnfmGenericMessage=(OrVnfmGenericMessage) message;
                    handleError(orVnfmGenericMessage.getVnfr());
                    nfvMessage = null;
                    break;
                case MODIFY:
                    orVnfmGenericMessage=(OrVnfmGenericMessage) message;
                    nfvMessage = getNfvMessage(Action.MODIFY, this.modify(orVnfmGenericMessage.getVnfr(), orVnfmGenericMessage.getVnfrd()));
                    break;
                case RELEASE_RESOURCES:
                    orVnfmGenericMessage=(OrVnfmGenericMessage) message;
                    nfvMessage = getNfvMessage(Action.RELEASE_RESOURCES, this.terminate(orVnfmGenericMessage.getVnfr()));
                    break;
                case INSTANTIATE:
                    OrVnfmInstantiateMessage orVnfmInstantiateMessage=(OrVnfmInstantiateMessage) message;
                    virtualNetworkFunctionRecord = createVirtualNetworkFunctionRecord(orVnfmInstantiateMessage.getVnfd(), orVnfmInstantiateMessage.getVnfdf().getFlavour_key() ,orVnfmInstantiateMessage.getVnfd().getName(), orVnfmInstantiateMessage.getVlrs(),orVnfmInstantiateMessage.getExtention());
                    virtualNetworkFunctionRecord = instantiate(virtualNetworkFunctionRecord);
                    nfvMessage = getNfvMessage(Action.INSTANTIATE, virtualNetworkFunctionRecord);
                    setupProvides(virtualNetworkFunctionRecord);
                    break;
                case SCALE_IN_FINISHED:
                    break;
                case SCALE_OUT_FINISHED:
                    break;
                case SCALE_UP_FINISHED:
                    break;
                case SCALE_DOWN_FINISHED:
                    break;
                case RELEASE_RESOURCES_FINISH:
                    break;
                case INSTANTIATE_FINISH:
                    break;
                case CONFIGURE:
                    orVnfmGenericMessage=(OrVnfmGenericMessage) message;
                    nfvMessage = getNfvMessage(Action.CONFIGURE, configure(orVnfmGenericMessage.getVnfr()));
                    break;
                case START:
                    orVnfmGenericMessage=(OrVnfmGenericMessage) message;
                    nfvMessage = getNfvMessage(Action.START, start(orVnfmGenericMessage.getVnfr()));
                    break;
            }

            log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            if (nfvMessage != null) {
                //coreMessage.setDependency(message.getDependency());
                log.debug("send to NFVO");
                sendToNfvo(nfvMessage);
            }
        } catch (Exception e) {
            log.error("ERROR: ", e);
            sendToNfvo(getNfvMessage(Action.ERROR, virtualNetworkFunctionRecord));
        }
    }

    protected abstract VirtualNetworkFunctionRecord grantLifecycleOperation(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VnfmSdkException;

    protected abstract VirtualNetworkFunctionRecord allocateResources(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VnfmSdkException;

    private void setupProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        fillSpecificProvides(virtualNetworkFunctionRecord);

        log.debug("Provides is: " + virtualNetworkFunctionRecord.getProvides());
        //TODO add common parameters, even not defined into the provides: i.e. ip (DONE ?)

        List<String> hostnames = new ArrayList<>();
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()){
            for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()){
                hostnames.add(vnfcInstance.getHostname());
            }
        }

        ConfigurationParameter cp = new ConfigurationParameter();
        cp.setConfKey(virtualNetworkFunctionRecord.getType() + ".ips");
        cp.setValue(virtualNetworkFunctionRecord.getVnf_address().toString());

        virtualNetworkFunctionRecord.getProvides().getConfigurationParameters().add(cp);

        ConfigurationParameter cp2 = new ConfigurationParameter();
        cp2.setConfKey(virtualNetworkFunctionRecord.getType() + ".hostnames");
        cp2.setValue(hostnames.toString());
        virtualNetworkFunctionRecord.getProvides().getConfigurationParameters().add(cp2);
        /**
         * Before ending, need to get all the "provides" filled
         *
         * TODO ask EMS for specific parameters
         *
         */

        log.debug("Provides is: " + virtualNetworkFunctionRecord.getProvides());
        for (ConfigurationParameter configurationParameter : virtualNetworkFunctionRecord.getProvides().getConfigurationParameters()){
            if (!configurationParameter.getConfKey().startsWith("#nfvo:")){
                log.debug(configurationParameter.getConfKey() + ": " + configurationParameter.getValue());
            }
        }

    }

    /**
     * This method needs to set all the parameter specified in the VNFDependency.parameters
     *
     * @param virtualNetworkFunctionRecord
     */
    protected void fillSpecificProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord){}

    /**
     * This method can be overwritten in case you want a specific initialization of the VirtualNetworkFunctionRecord from the VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor
     * @param extension
     * @return The new VirtualNetworkFunctionRecord
     * @throws BadFormatException
     * @throws NotFoundException
     */
    protected VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String flavourId, String vnfInstanceName, Set<VirtualLinkRecord> virtualLink, Map<String, String> extension ) throws BadFormatException, NotFoundException {
        try {
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = VNFRUtils.createVirtualNetworkFunctionRecord(virtualNetworkFunctionDescriptor, flavourId, extension.get("nsr-id"));
            log.debug("Created VirtualNetworkFunctionRecord: " + virtualNetworkFunctionRecord);
            return virtualNetworkFunctionRecord;
        } catch (NotFoundException e) {
            e.printStackTrace();
            sendToNfvo(getNfvMessage(Action.ERROR, null));
            throw e;
        } catch (BadFormatException e) {
            e.printStackTrace();
            sendToNfvo(getNfvMessage(Action.ERROR, null));
            throw e;
        }
    }

    protected abstract VirtualNetworkFunctionRecord start(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

    protected LifecycleEvent getLifecycleEvent(Collection<LifecycleEvent> events, Event event) {
        for (LifecycleEvent lce : events)
            if (lce.getEvent().ordinal() == event.ordinal()) {
                return lce;
            }
        return null;
    }

    protected NFVMessage getNfvMessage(Action action, VirtualNetworkFunctionRecord payload) {
        NFVMessage nfvMessage= null;
        if(Action.INSTANTIATE.ordinal()==action.ordinal())
            nfvMessage = new VnfmOrInstantiateMessage(payload);
        else
            nfvMessage = new VnfmOrGenericMessage(payload,action);
        return nfvMessage;
    }

    /**
     * This method can be used when an Event is processed and concluded in the main Class of the VNFM
     *
     * @param virtualNetworkFunctionRecord the VNFR
     * @param event                        the EVENT to be put in the history
     */
    protected void updateVnfr(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event) {
        for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event())
            if (lifecycleEvent.getEvent().ordinal() == event.ordinal()) {
                virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
                break;
            }
    }

    /**
     * This method is used when a command is executed in the EMS, in order to put the executed command in history
     * lifecycle events. In case of error is easy to understand what was the last command correctly executed.
     *
     * @param vnfr    the VNFR
     * @param event   the EVENT containing the command
     * @param command the command executed
     */
    protected void updateVnfr(VirtualNetworkFunctionRecord vnfr, Event event, String command) {
        if (vnfr == null || event == null || command == null || command.isEmpty())
            throw new NullPointerException("One of the arguments is null or the command is empty");

        //Change vnfr status if the current command is the last script of the current event.
        LifecycleEvent currentEvent = getLifecycleEvent(vnfr.getLifecycle_event(), event);
        String lastScript = (String) currentEvent.getLifecycle_events().toArray()[currentEvent.getLifecycle_events().size() - 1];
        log.debug("Last script is: " + lastScript);

        //set the command in the history event
        LifecycleEvent historyEvent = getLifecycleEvent(vnfr.getLifecycle_event_history(), event);
        if (historyEvent != null)
            historyEvent.getLifecycle_events().add(command);
            // If the history event doesn't exist create it
        else {
            LifecycleEvent newLce = new LifecycleEvent();
            newLce.setEvent(event);
            newLce.setLifecycle_events(new LinkedHashSet<String>());
            newLce.getLifecycle_events().add(command);
            vnfr.getLifecycle_event_history().add(newLce);
        }
    }

    protected abstract String executeActionOnEMS(String vduHostname, String command) throws Exception;

    protected abstract VirtualNetworkFunctionRecord configure(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;

    protected abstract void sendToNfvo(final NFVMessage coreMessage);

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

    protected void sendToEmsAndUpdate(VirtualNetworkFunctionRecord vnfr, Event event, String command, String emsEndpoint) throws Exception {
        executeActionOnEMS(emsEndpoint, command);
        try {
            updateVnfr(vnfr, event, command);
            log.debug("Updated VNFR");
        } catch (NullPointerException e) {
            throw new VnfmSdkException(e);
        }
    }
}
