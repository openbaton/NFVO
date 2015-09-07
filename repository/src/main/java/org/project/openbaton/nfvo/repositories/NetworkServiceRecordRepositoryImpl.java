package org.project.openbaton.nfvo.repositories;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by mob on 03.09.15.
 */
@Transactional(readOnly = true)
public class NetworkServiceRecordRepositoryImpl implements NetworkServiceRecordRepositoryCustom {

    @Autowired
    private NetworkServiceRecordRepository networkServiceRecordRepository;

    @Autowired
    private VNFRRepository vnfrRepository;

    @Override
    @Transactional
    public void addVnfr(VirtualNetworkFunctionRecord vnfr, String id) {
        vnfr=vnfrRepository.save(vnfr);
        networkServiceRecordRepository.findFirstById(id).getVnfr().add(vnfr);
    }
}
