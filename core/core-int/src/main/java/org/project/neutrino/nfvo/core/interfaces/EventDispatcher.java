package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.EventEndpoint;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;

/**
 * Created by lto on 01/07/15.
 */
public interface EventDispatcher {
    void register(EventEndpoint endpoint);

    void unregister(String name) throws NotFoundException;
}
