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

package org.openbaton.nfvo.security.interfaces;

import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;

/** Created by lto on 24/05/16. */
public interface ProjectManagement {
  /** @param project */
  Project add(Project project);

  /** @param project */
  void delete(Project project)
      throws EntityInUseException, NotAllowedException, BadRequestException, NotFoundException;

  /** @param new_project */
  Project update(Project new_project) throws NotAllowedException, NotFoundException;

  /** */
  Iterable<Project> query();

  /** @param id */
  Project query(String id) throws NotFoundException;

  /**
   * @param name
   * @return
   */
  Project queryByName(String name);

  /**
   * @param user
   * @return all Projects assigned to the User
   */
  Iterable<Project> query(User user);

  boolean exist(String project);
}
