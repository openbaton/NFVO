package org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts;

import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.util.EventFinishEvent;
import org.project.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.project.openbaton.nfvo.repositories.VNFRRepository;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */

/**
 * Putting these annotations only here won't work.
 */
@Service
@Scope("prototype")
public abstract class AbstractTask implements Runnable, ApplicationEventPublisherAware {
    protected Logger log = LoggerFactory.getLogger(AbstractTask.class);

    @Autowired
    private ConfigurableApplicationContext context;
    private ApplicationEventPublisher publisher;
    protected Action action;



    protected String tempDestination;
    protected VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    protected VNFRecordDependency dependency;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    @Autowired
    protected VNFRRepository vnfrRepository;

    @Autowired
    protected NetworkServiceRecordRepository networkServiceRecordRepository;


    @Override
    public void run() {
        changeStatus();
        try {
            this.doWork();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        /**
         * Send event finish
         */
        EventFinishEvent eventFinishEvent = new EventFinishEvent(this);
        eventFinishEvent.setAction(action);
        eventFinishEvent.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
        this.publisher.publishEvent(eventFinishEvent);
    }

    protected abstract void doWork() throws Exception;

    public boolean isAsync(){
        return true;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    protected VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException {
        String senderName = endpointType.toString().toLowerCase() + "VnfmSender";
        return (VnfmSender) this.context.getBean(senderName);
    }

    protected String getTempDestination() {
        return tempDestination;
    }

    public void setTempDestination(String tempDestination) {
        this.tempDestination = tempDestination;
    }
    protected void changeStatus() {
        log.debug("Action is: " + action);
        Status status = null;
        switch (action){
            case ALLOCATE_RESOURCES:
                status = Status.NULL;
                break;
            case SCALE:
                break;
            case SCALING:
                status = Status.SCALING;
                break;
            case ERROR:
                status = Status.ERROR;
                break;
            case MODIFY:
                 status = Status.INACTIVE;
                break;
            case RELEASE_RESOURCES:
                status = Status.TERMINATED;
                break;
            case GRANT_OPERATION:
                status = Status.NULL;
                break;
            case INSTANTIATE:
                status = Status.INITIALIZED;
                break;
            case SCALE_OUT_FINISHED:
                status = Status.ACTIVE;
                break;
            case SCALE_IN_FINISHED:
                status = Status.ACTIVE;
                break;
            case SCALE_UP_FINISHED:
                status = Status.ACTIVE;
                break;
            case SCALE_DOWN_FINISHED:
                status = Status.ACTIVE;
                break;
            case RELEASE_RESOURCES_FINISH:
                status = Status.TERMINATED;
                break;
            case INSTANTIATE_FINISH:
                status = Status.ACTIVE;
                break;
            case CONFIGURE:
                break;
            case START:
                status = Status.ACTIVE;
                break;
        }
        virtualNetworkFunctionRecord.setStatus(status);
        log.debug("Changing status of VNFR: " + virtualNetworkFunctionRecord.getName() + " ( " + virtualNetworkFunctionRecord.getId() + " ) to " + status);
    }

    public void setDependency(VNFRecordDependency dependency) {
        this.dependency = dependency;
    }
}
