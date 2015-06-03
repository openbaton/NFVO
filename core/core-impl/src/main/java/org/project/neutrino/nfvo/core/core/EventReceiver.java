package org.project.neutrino.nfvo.core.core;

/**
 * Created by lto on 03/06/15.
 */

import org.project.neutrino.nfvo.catalogue.nfvo.ApplicationEventNFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class EventReceiver implements ApplicationListener<ApplicationEventNFVO> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onApplicationEvent(ApplicationEventNFVO  event) {
        log.debug("Received event: " + event);
    }
}
