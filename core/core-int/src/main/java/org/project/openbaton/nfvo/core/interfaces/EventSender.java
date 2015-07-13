package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.common.catalogue.nfvo.ApplicationEventNFVO;
import org.project.openbaton.common.catalogue.nfvo.EventEndpoint;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by lto on 01/07/15.
 */
public interface EventSender {
    Future<Void> send(EventEndpoint endpoint, ApplicationEventNFVO event) throws IOException;
}
