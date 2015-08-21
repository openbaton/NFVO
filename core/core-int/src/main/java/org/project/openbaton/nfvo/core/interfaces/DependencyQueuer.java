package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.nfvo.common.exceptions.NotFoundException;

import java.util.Set;

/**
 * Created by lto on 19/08/15.
 */
public interface DependencyQueuer {
    void waitForVNFR(String targetDependencyId, Set<String> sourceNames) throws InterruptedException, NotFoundException;

    void releaseVNFR(String vnfrId) throws NotFoundException;
}
