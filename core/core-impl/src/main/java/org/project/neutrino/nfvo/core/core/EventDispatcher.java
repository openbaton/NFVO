package org.project.neutrino.nfvo.core.core;

/**
 * Created by lto on 03/06/15.
 */

import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.ApplicationEventNFVO;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;

@Component
class EventDispatcher implements ApplicationListener<ApplicationEventNFVO> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DependencyManagement dependencyManagement;

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
    }
}
