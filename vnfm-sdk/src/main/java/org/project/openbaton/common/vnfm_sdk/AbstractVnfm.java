package org.project.openbaton.common.vnfm_sdk;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

/**
 * Created by lto on 08/07/15.
 */
public abstract class AbstractVnfm implements VNFLifecycleManagement, VNFLifecycleChangeNotification {
    protected static final String nfvoQueue = "vnfm-core-actions";
    protected String type;
    protected String endpoint;
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
    public abstract VirtualNetworkFunctionRecord modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFRecordDependency dependency);

    @Override
    public abstract void upgradeSoftware();

    @Override
    public abstract CoreMessage terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

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
    }

    protected void onAction(CoreMessage message) throws NotFoundException, BadFormatException {
        log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + message.getAction() + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        log.trace("VNFM: Received Message: " + message.getAction());
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = message.getVirtualNetworkFunctionRecord();
        CoreMessage coreMessage = null;
        switch (message.getAction()) {
            case SCALE:
                this.scale();
                break;
            case SCALING:
                break;
            case ERROR:
                coreMessage = handleError(virtualNetworkFunctionRecord);
                break;
            case MODIFY:
                coreMessage.setVirtualNetworkFunctionRecord(this.modify(virtualNetworkFunctionRecord, message.getDependency()));
                break;
            case RELEASE_RESOURCES:
                coreMessage = this.terminate(virtualNetworkFunctionRecord);
                break;
            case INSTANTIATE:
                message.setVirtualNetworkFunctionRecord(createVirtualNetworkFunctionRecord(message.getVirtualNetworkFunctionDescriptor(), message.getExtention()));
            case ALLOCATE_RESOURCES:
            case GRANT_OPERATION:
                virtualNetworkFunctionRecord = message.getVirtualNetworkFunctionRecord();
                virtualNetworkFunctionRecord = instantiate(virtualNetworkFunctionRecord);
                if (virtualNetworkFunctionRecord != null)
                    coreMessage = getCoreMessage(Action.INSTANTIATE, virtualNetworkFunctionRecord);
                else
                    coreMessage = null;
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
                coreMessage = configure(virtualNetworkFunctionRecord);
                break;
            case START:
                coreMessage = start(virtualNetworkFunctionRecord);
                break;
        }

        log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        if (coreMessage != null) {
            coreMessage.setDependency(message.getDependency());
            log.debug("send to NFVO");
            sendToNfvo(coreMessage);
        }
    }

    /**
     * This method can be overwritten in case you want a specific initialization of the VirtualNetworkFunctionRecord from the VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor
     * @param extention
     * @return The new VirtualNetworkFunctionRecord
     * @throws BadFormatException
     * @throws NotFoundException
     */
    protected VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, Map<String, String> extention) throws BadFormatException, NotFoundException {
        try {
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = VNFRUtils.createVirtualNetworkFunctionRecord(virtualNetworkFunctionDescriptor, extention.get("nsr-id"));
            log.debug("Created VirtualNetworkFunctionRecord: " + virtualNetworkFunctionRecord);
            return virtualNetworkFunctionRecord;
        } catch (NotFoundException e) {
            e.printStackTrace();
            sendToNfvo(getCoreMessage(Action.ERROR, null));
            throw e;
        } catch (BadFormatException e) {
            e.printStackTrace();
            sendToNfvo(getCoreMessage(Action.ERROR, null));
            throw e;
        }
    }

    protected abstract CoreMessage start(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    protected LifecycleEvent getLifecycleEvent(Collection<LifecycleEvent> events, Event event) {
        for (LifecycleEvent lce : events)
            if (lce.getEvent().ordinal() == event.ordinal()) {
                return lce;
            }
        return null;
    }

    protected CoreMessage getCoreMessage(Action action, VirtualNetworkFunctionRecord payload) {
        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(action);
        coreMessage.setVirtualNetworkFunctionRecord(payload);
        return coreMessage;
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

    protected abstract CoreMessage configure(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    protected abstract void sendToNfvo(CoreMessage coreMessage);

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
        vnfmManagerEndpoint.setEndpointType(EndpointType.JMS);
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
