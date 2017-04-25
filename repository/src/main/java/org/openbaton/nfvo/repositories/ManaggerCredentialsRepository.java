package org.openbaton.nfvo.repositories;

import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by lto on 25.04.17.
 */
public interface ManaggerCredentialsRepository extends CrudRepository<ManagerCredentials, String> {
  ManagerCredentials findFirstById(String id);

  ManagerCredentials findFirstByRabbitUsername(String username);
}
