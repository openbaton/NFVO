package org.project.openbaton.common.vnfm_sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.project.openbaton.common.vnfm_sdk.interfaces.VNFLifecycleManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by lto on 08/07/15.
 */
public abstract class AbstractVnfm implements VNFLifecycleManagement {
    protected String type;
    protected String endpoint;
    protected Properties properties;
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected VnfmManagerEndpoint vnfmManagerEndpoint;
    protected static final String nfvoQueue = "vnfm-core-actions";
    protected Gson parser=new GsonBuilder().setPrettyPrinting().create();

    @PreDestroy
    private void shutdown(){
        this.unregister(vnfmManagerEndpoint);
    }

    @PostConstruct
    private void init(){
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
    public abstract CoreMessage instantiate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    @Override
    public abstract void query();

    @Override
    public abstract void scale(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    @Override
    public abstract void checkInstantiationFeasibility();

    @Override
    public abstract void heal();

    @Override
    public abstract void updateSoftware();

    @Override
    public abstract CoreMessage modify(VirtualNetworkFunctionRecord vnfr);

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

    protected void onAction(CoreMessage message) {
        log.trace("VNFM: Received Message: " + message.getAction());
        CoreMessage coreMessage = null;
        switch (message.getAction()){
            case ALLOCATE_RESOURCES:
                break;
            case SCALE:
                this.scale(message.getPayload());
                break;
            case SCALING:
                break;
            case ERROR:
                coreMessage = handleError(message.getPayload());
                break;
            case MODIFY:
                coreMessage = this.modify(message.getPayload());
                break;
            case RELEASE_RESOURCES:
                coreMessage = this.terminate(message.getPayload());
                break;
            case GRANT_OPERATION:
            case INSTANTIATE:
                coreMessage = this.instantiate(message.getPayload());
            case SCALE_UP_FINISHED:
                break;
            case SCALE_DOWN_FINISHED:
                break;
            case RELEASE_RESOURCES_FINISH:
                break;
            case INSTANTIATE_FINISH:
                break;
            case CONFIGURE:
                coreMessage = configure(message.getPayload());
                break;
            case START:
                coreMessage = start(message.getPayload());
                break;
        }


        if (coreMessage != null){
            log.debug("send to NFVO");
            sendToNfvo(coreMessage);
        }
    }
    private LifecycleEvent getLifecycleEvent(VirtualNetworkFunctionRecord vnfr,Event event,boolean history){
        if(history){
            for( LifecycleEvent lce : vnfr.getLifecycle_event_history())
                if(lce.getEvent()==event){
                    return lce;
                }
        }
        else for( LifecycleEvent lce : vnfr.getLifecycle_event())
                    if(lce.getEvent()==event){
                        return lce;
                    }
        return null;
    }
    protected void updateVnfr(VirtualNetworkFunctionRecord vnfr, Event event,String command){
        if(vnfr==null || event==null || command==null || command.isEmpty())
            throw new NullPointerException("One of the arguments is null or the command is empty");

        //Change vnfr status if the current command is the last script of the current event.
        LifecycleEvent currentEvent= getLifecycleEvent(vnfr,event,false);
        String lastScript=null;
        while(currentEvent.getLifecycle_events().iterator().hasNext())
            lastScript=currentEvent.getLifecycle_events().iterator().next();
        if(lastScript.equalsIgnoreCase(command))
            changeStatus(vnfr,currentEvent.getEvent());

        //If the current vnfr is INITIALIZED and it hasn't a configure event, set it as INACTIVE
        if(vnfr.getStatus()==Status.INITIAILZED && getLifecycleEvent(vnfr,Event.CONFIGURE,false)==null)
            changeStatus(vnfr,Event.CONFIGURE);

        //set the command in the history event
        LifecycleEvent historyEvent = getLifecycleEvent(vnfr,event,true);
        if(historyEvent!=null)
            historyEvent.getLifecycle_events().add(command);
        // If the history event doesn't exist create it
        else{
            LifecycleEvent newLce = new LifecycleEvent();
            newLce.setEvent(event);
            newLce.getLifecycle_events().add(command);
            vnfr.getLifecycle_event_history().add(newLce);
        }
    }

    private void changeStatus(VirtualNetworkFunctionRecord vnfr, Event event) {
        switch (event){
            case INSTANTIATE: vnfr.setStatus(Status.INITIAILZED);
                break;
            case TERMINATE: vnfr.setStatus(Status.TERMINATED);
                break;
            case CONFIGURE: vnfr.setStatus(Status.INACTIVE);
                break;
            case START: vnfr.setStatus(Status.ACTIVE);
                break;
            case STOP: vnfr.setStatus(Status.INACTIVE);
                break;
            case UPDATE:
                break;
            case UPDATE_ROLLBACK:
                break;
            case UPGRADE:
                break;
            case UPGRADE_ROLLBACK:
                break;
            case RESET:
                break;
        }

    }

    protected abstract void executeActionOnEMS(String vduHostname, String command) throws JMSException, VnfmSdkException;

    protected abstract CoreMessage start(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    private LifecycleEvent getLifecycleEvent(VirtualNetworkFunctionRecord vnfr,Event event,boolean history){
        if(history){
            for( LifecycleEvent lce : vnfr.getLifecycle_event_history())
                if(lce.getEvent()==event){
                    return lce;
                }
        }
        else for( LifecycleEvent lce : vnfr.getLifecycle_event())
                    if(lce.getEvent()==event){
                        return lce;
                    }
        return null;
    }
    protected void updateVnfr(VirtualNetworkFunctionRecord vnfr, Event event,String command){
        if(vnfr==null || event==null || command==null || command.isEmpty())
            throw new NullPointerException("One of the arguments is null or the command is empty");

        //Change vnfr status if the current command is the last script of the current event.
        LifecycleEvent currentEvent= getLifecycleEvent(vnfr,event,false);
        String lastScript=null;
        while(currentEvent.getLifecycle_events().iterator().hasNext())
            lastScript=currentEvent.getLifecycle_events().iterator().next();
        if(lastScript.equalsIgnoreCase(command))
            changeStatus(vnfr,currentEvent.getEvent());

        //If the current vnfr is INITIALIZED and it hasn't a configure event, set it as INACTIVE
        if(vnfr.getStatus()==Status.INITIAILZED && getLifecycleEvent(vnfr,Event.CONFIGURE,false)==null)
            changeStatus(vnfr,Event.CONFIGURE);

        //set the command in the history event
        LifecycleEvent historyEvent = getLifecycleEvent(vnfr,event,true);
        if(historyEvent!=null)
            historyEvent.getLifecycle_events().add(command);
        // If the history event doesn't exist create it
        else{
            LifecycleEvent newLce = new LifecycleEvent();
            newLce.setEvent(event);
            newLce.getLifecycle_events().add(command);
            vnfr.getLifecycle_event_history().add(newLce);
        }
    }

    private void changeStatus(VirtualNetworkFunctionRecord vnfr, Event event) {
        switch (event){
            case GRANTED:
                break;
            case ALLOCATE:
                break;
            case INSTALL:
                break;
            case SCALE:
                break;
            case RELEASE:
                break;
            case ERROR:
                break;
            case INSTANTIATE: vnfr.setStatus(Status.INITIAILZED);
                break;
            case TERMINATE: vnfr.setStatus(Status.TERMINATED);
                break;
            case CONFIGURE: vnfr.setStatus(Status.INACTIVE);
                break;
            case START: vnfr.setStatus(Status.ACTIVE);
                break;
            case STOP: vnfr.setStatus(Status.INACTIVE);
                break;
            case SCALE_OUT:
                break;
            case SCALE_IN:
                break;
            case SCALE_UP:
                break;
            case SCALE_DOWN:
                break;
            case UPDATE:
                break;
            case UPDATE_ROLLBACK:
                break;
            case UPGRADE:
                break;
            case UPGRADE_ROLLBACK:
                break;
            case RESET:
                break;
        }

    }

    protected abstract void executeActionOnEMS(String vduHostname, String command) throws JMSException, VnfmSdkException;

    protected abstract CoreMessage configure(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

    protected abstract void sendToNfvo(CoreMessage coreMessage);

    protected abstract void unregister(VnfmManagerEndpoint endpoint);

    protected abstract void setup();

    protected void sendToEmsAndUpdate(VirtualNetworkFunctionRecord vnfr, Event event, String command, String emsEndpoint) throws VnfmSdkException, JMSException {
        executeActionOnEMS(emsEndpoint, command);
        updateVnfr(vnfr, event, command);
    }
}
