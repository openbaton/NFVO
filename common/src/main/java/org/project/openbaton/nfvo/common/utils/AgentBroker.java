package org.project.openbaton.nfvo.common.utils;

import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.nfvo.common.interfaces.Receiver;
import org.project.openbaton.nfvo.common.interfaces.Sender;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by tce on 14.08.15.
 */
@Service
@Scope
public class AgentBroker {
    public Sender getSender(EndpointType endpointType) {
        return null;
    }

    public Receiver getReceiver(EndpointType endpointType) {
        return null;
    }
}
