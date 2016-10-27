package org.openbaton.nfvo.repositories;

import org.openbaton.catalogue.security.HistoryEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by lto on 17/10/16.
 */
public interface HistoryEntityRepository extends CrudRepository<HistoryEntity, String> {
  HistoryEntity[] findByUsername(String username);

  HistoryEntity[] findAll(Sort sort);
}
