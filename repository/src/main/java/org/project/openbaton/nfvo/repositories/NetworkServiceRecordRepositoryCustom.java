package org.project.openbaton.nfvo.repositories;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

/**
 * Created by mob on 04.09.15.
 */
public interface NetworkServiceRecordRepositoryCustom {
    VirtualNetworkFunctionRecord addVnfr(VirtualNetworkFunctionRecord vnfr, String id);
}
