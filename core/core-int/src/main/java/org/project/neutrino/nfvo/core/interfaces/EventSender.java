package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.ApplicationEventNFVO;
import org.project.neutrino.nfvo.catalogue.nfvo.EventEndpoint;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by lto on 01/07/15.
 */
public interface EventSender {
    Future<Void> send(EventEndpoint endpoint, ApplicationEventNFVO event) throws IOException;
}
