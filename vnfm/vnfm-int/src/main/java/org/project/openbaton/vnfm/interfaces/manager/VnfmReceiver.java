package org.project.openbaton.vnfm.interfaces.manager;

import org.project.openbaton.common.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 08/07/15.
 */
public interface VnfmReceiver {

    @JmsListener(destination = "vnfm-core-actions", containerFactory = "queueJmsContainerFactory")
    void actionFinished(@Payload CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException, VimException;
}
