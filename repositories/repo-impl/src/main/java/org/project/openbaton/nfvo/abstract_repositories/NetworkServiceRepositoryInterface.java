package org.project.openbaton.nfvo.abstract_repositories;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;

/**
 * Created by mob on 04.09.15.
 */
public interface NetworkServiceRepositoryInterface extends GenericRepository<NetworkServiceRecord> {
    void addVnfr(VirtualNetworkFunctionRecord vnfr, String id);
}
