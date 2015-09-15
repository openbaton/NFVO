package org.project.openbaton.nfvo.repositories;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.Status;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 03.09.15.
 */
public interface NetworkServiceRecordRepository extends CrudRepository<NetworkServiceRecord,String>,NetworkServiceRecordRepositoryCustom {
    NetworkServiceRecord findFirstById(String id);

}
