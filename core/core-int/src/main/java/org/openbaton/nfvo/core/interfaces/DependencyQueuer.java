package org.openbaton.nfvo.core.interfaces;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.exceptions.NotFoundException;

import java.util.Set;

/**
 * Created by lto on 19/08/15.
 */
public interface DependencyQueuer {
    void waitForVNFR(String targetDependencyId, Set<String> sourceNames) throws InterruptedException, NotFoundException;

    void releaseVNFR(String vnfrId, NetworkServiceRecord nsr) throws NotFoundException;
}
