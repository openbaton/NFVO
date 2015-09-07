package org.project.openbaton.nfvo.repositories;

import org.project.openbaton.catalogue.nfvo.Configuration;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 05.09.15.
 */
public interface ConfigurationRepository extends CrudRepository<Configuration,String> {
}
