package org.project.openbaton.nfvo.vnfm_reg.impl.receiver;

import org.project.openbaton.common.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 26/05/15.
 */
@Service
@Scope
public class VnfmReceiverJMS implements VnfmReceiver {

    @Autowired
    private org.project.openbaton.vnfm.interfaces.manager.VnfmManager vnfmManager;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @JmsListener(destination = "vnfm-core-actions", containerFactory = "queueJmsContainerFactory")
    public void actionFinished(@Payload CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException, VimException {
        log.debug("CORE: Received: " + coreMessage);

        vnfmManager.executeAction(coreMessage);

    }

}
