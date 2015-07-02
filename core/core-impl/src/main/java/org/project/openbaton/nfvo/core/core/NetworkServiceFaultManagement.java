package org.project.openbaton.nfvo.core.core;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
public class NetworkServiceFaultManagement implements org.project.openbaton.nfvo.core.interfaces.NetworkServiceFaultManagement {
    @Override
    public void notifyFault() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getFaultInformation() {
        throw new UnsupportedOperationException();
    }
}
