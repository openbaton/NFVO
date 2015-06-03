package org.project.neutrino.vnfm.interfaces.manager;

import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmManager {
    Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException;

    @JmsListener(destination = "vnfm-core-actions", containerFactory = "myJmsContainerFactory")
    void actionFinished(@Payload CoreMessage coreMessage);
}
