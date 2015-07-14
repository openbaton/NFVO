package org.project.openbaton.nfvo.core.events;

/**
 * Created by lto on 03/06/15.
 */

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.core.core.DependencyManagement;
import org.project.openbaton.nfvo.core.interfaces.EventSender;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;

@Component
class EventDispatcher implements ApplicationListener<ApplicationEventNFVO>, org.project.openbaton.nfvo.core.interfaces.EventDispatcher {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DependencyManagement dependencyManagement;

    @Autowired
    @Qualifier("eventEndpointRepository")
    private GenericRepository<EventEndpoint> eventEndpointRepository;

    @Autowired
    private ConfigurableApplicationContext context;

    @Override
    public void onApplicationEvent(ApplicationEventNFVO  event) {
        log.debug("Received event: " + event);
        switch (event.getAction()){
            case INSTANTIATE_FINISH:
                VirtualNetworkFunctionRecord vnfr = (VirtualNetworkFunctionRecord) event.getPayload();
                log.trace("Instantiate is finished for VNFR: " + vnfr);
                log.debug("Instantiate is finished for VNFR: " + vnfr.getName());
                log.debug("Calling dependency management for VNFR: " + vnfr.getName());
                try {
                    dependencyManagement.provisionDependencies(vnfr);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
                break;
            case ALLOCATE_RESOURCES:
                break;
            case ERROR:
                break;
            case RELEASE_RESOURCES:
                break;
            case INSTANTIATE:
                break;
            case MODIFY:
                break;
        }

        dispatchEvent(event);
    }

    private void dispatchEvent(ApplicationEventNFVO event){
        log.debug("dispatching event to the world!!!");

        List<EventEndpoint> endpoints = eventEndpointRepository.findAll();

        for (EventEndpoint endpoint : endpoints){
            if (endpoint.getEvent().ordinal() == event.getAction().ordinal()){
                if (endpoint.getVirtualNetworkFunctionId() != null){
                    if (event.getPayload() instanceof VirtualNetworkFunctionRecord){
                        if (((VirtualNetworkFunctionRecord) event.getPayload()).getId().equals(endpoint.getVirtualNetworkFunctionId())){
                            sendEvent(endpoint,event);
                        }
                    }
                }
                else if (endpoint.getNetworkServiceId() != null){
                    if (event.getPayload() instanceof NetworkServiceRecord){
                        if (((NetworkServiceRecord) event.getPayload()).getId().equals(endpoint.getNetworkServiceId())){
                            sendEvent(endpoint,event);
                        }
                    }
                }else {
                    log.debug("dispatching event to: " + endpoint);
                    sendEvent(endpoint, event);
                }
            }
        }

    }

    private void sendEvent(EventEndpoint endpoint, ApplicationEventNFVO event) {
        EventSender sender = (EventSender) context.getBean(endpoint.getType().toString().toLowerCase() + "EventSender");

        try {
            sender.send(endpoint, event);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while dispatching event " + event);
        }
    }

    @Override
    public void register(EventEndpoint endpoint){
        eventEndpointRepository.create(endpoint);
    }

    @Override
    public void unregister(String name) throws NotFoundException {
        for (EventEndpoint endpoint: eventEndpointRepository.findAll()){
            if (endpoint.getName().equals(name)){
                eventEndpointRepository.remove(endpoint);
                return;
            }
        }
        throw new NotFoundException("EventEndpoint with name " + name + " not found");
    }

}
