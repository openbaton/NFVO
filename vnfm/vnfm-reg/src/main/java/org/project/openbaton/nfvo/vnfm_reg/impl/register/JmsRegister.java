package org.project.openbaton.nfvo.vnfm_reg.impl.register;

import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Created by lto on 27/05/15.
 */
@Component
public class JmsRegister extends VnfmRegister {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @JmsListener(destination = "vnfm-register", containerFactory = "queueJmsContainerFactory")
    public void addManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        if (endpoint.getEndpointType() == null){
            endpoint.setEndpointType(EndpointType.JMS);
        }
        log.debug("Received: " + endpoint);
        this.register(endpoint);
    }

    @Override
    @JmsListener(destination = "vnfm-unregister", containerFactory = "queueJmsContainerFactory")
    public void removeManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        log.debug("Unregistering: " + endpoint);
        this.unregister(endpoint);
    }


}
