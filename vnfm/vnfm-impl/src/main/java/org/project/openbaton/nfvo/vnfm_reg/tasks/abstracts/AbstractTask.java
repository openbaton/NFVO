package org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.util.EventFinishEvent;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    protected VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

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
    @Qualifier("VNFRRepository")
    protected GenericRepository<VirtualNetworkFunctionRecord> vnfrRepository;


    @Override
    public void run() {
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
        String senderName = endpointType.toString().toLowerCase() + "Sender";
        return (VnfmSender) this.context.getBean(senderName);
    }
}
