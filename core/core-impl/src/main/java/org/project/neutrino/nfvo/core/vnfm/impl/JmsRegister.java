package org.project.neutrino.nfvo.core.vnfm.impl;

import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.core.vnfm.VnfmRegister;
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
    @JmsListener(destination = "vnfm-register")
    public void addManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        if (endpoint.getType() == null || endpoint.getType().length() == 0){
            endpoint.setType("jms");
        }
        this.register(endpoint);
    }
}
