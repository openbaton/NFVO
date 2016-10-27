/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.exceptions.NotFoundException;

/**
 * Created by lto on 13/05/15.
 */
public interface ConfigurationManagement {

  /**
   * This operation allows adding a datacenter into the datacenter repository.
   *
   * @param datacenter
   */
  Configuration add(Configuration datacenter);

  /**
   * This operation allows deleting the datacenter from the datacenter repository.
   *
   * @param id
   */
  void delete(String id);

  /**
   * This operation allows updating the datacenter in the datacenter repository.
   *
   * @param new_datacenter
   * @param id
   * @param projectId
   */
  Configuration update(Configuration new_datacenter, String id, String projectId);

  /**
   * This operation allows querying the information of the datacenters in the datacenter repository.
   */
  Iterable<Configuration> query();

  Iterable<Configuration> queryByProject(String projectId);

  /**
   * This operation allows querying the information of the datacenter in the datacenter repository.
   */
  Configuration query(String id, String projectId);

  Configuration queryByName(String system) throws NotFoundException;
}
