package org.project.openbaton.nfvo.repositories;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
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
    public VirtualNetworkFunctionRecord addVnfr(VirtualNetworkFunctionRecord vnfr, String id) {
        vnfr=vnfrRepository.save(vnfr);
        networkServiceRecordRepository.findFirstById(id).getVnfr().add(vnfr);
        return vnfr;
    }

    @Override
    @Transactional
    public void deleteVNFRecord(String idNsr, String idVnfd) {
        networkServiceRecordRepository.findFirstById(idNsr).getVnfr().remove(vnfrRepository.findOne(idVnfd));
    }
}
