package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.nfvo.exceptions.NotFoundException;

/**
 * Created by lto on 01/07/15.
 */
public interface EventDispatcher {
    void register(EventEndpoint endpoint);

    void unregister(String name) throws NotFoundException;
}
