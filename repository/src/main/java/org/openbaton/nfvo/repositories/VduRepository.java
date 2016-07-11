package org.openbaton.nfvo.repositories;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by lto on 13/10/15.
 */
public interface VduRepository extends CrudRepository<VirtualDeploymentUnit, String> {
  List<VirtualDeploymentUnit> findByProjectId(String projectId);
}
