package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;

/**
 * Created by lto on 19/08/15.
 */
public interface DependencyQueuer {
    void waitForVNFR(String vnfrSourceId, VNFRecordDependency dependency) throws InterruptedException, NotFoundException;

    void releaseVNFR(String vnfrId) throws NotFoundException;

    boolean resolvedDependencies(String networkServiceId);

    int calculateDependencies(String virtualNetworkFunctionRecordId);
}
