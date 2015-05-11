package org.project.neutrino.nfvo.core.core;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
public class NetworkServiceFaultManagement implements org.project.neutrino.nfvo.core.interfaces.NetworkServiceFaultManagement {
    @Override
    public void notifyFault() {
        throw new NotImplementedException();
    }

    @Override
    public void getFaultInformation() {
        throw new NotImplementedException();
    }
}
