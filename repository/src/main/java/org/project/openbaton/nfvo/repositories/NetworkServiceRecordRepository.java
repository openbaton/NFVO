package org.project.openbaton.nfvo.repositories;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 03.09.15.
 */
public interface NetworkServiceRecordRepository extends CrudRepository<NetworkServiceRecord,String>,NetworkServiceRecordRepositoryCustom {
    NetworkServiceRecord findFirstById(String id);
}
