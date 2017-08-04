package org.openbaton.nfvo.repositories;

import org.openbaton.catalogue.nfvo.ServiceMetadata;
import org.springframework.data.repository.CrudRepository;

/** Created by lto on 04/04/2017. */
public interface ServiceRepository extends CrudRepository<ServiceMetadata, String> {
  ServiceMetadata findByName(String name);

  ServiceMetadata findById(String id);
}
