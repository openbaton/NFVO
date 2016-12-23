/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.api.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.security.interfaces.ProjectManagement;
import org.openbaton.nfvo.security.interfaces.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Created by lto on 25/05/16. */
@RestController
@RequestMapping("/api/v1/projects")
public class RestProject {
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ProjectManagement projectManagement;
  @Autowired private UserManagement userManagement;

  /**
   * Adds a new Project to the Projects repository
   *
   * @param project
   * @return project
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public Project create(@RequestBody @Valid Project project)
      throws NotAllowedException, NotFoundException {
    log.info("Adding Project: " + project.getName());
    if (isAdmin()) {
      return projectManagement.add(project);
    } else {
      throw new NotAllowedException("Forbidden to create project " + project.getName());
    }
  }

  /**
   * Removes the Project from the Projects repository
   *
   * @param id : the id of project to be removed
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") String id)
      throws NotAllowedException, NotFoundException, EntityInUseException, BadRequestException {
    log.info("Removing Project with id " + id);
    if (isAdmin()) {
      projectManagement.delete(projectManagement.query(id));
    } else {
      throw new NotAllowedException("Forbidden to delete project " + id);
    }
  }

  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(@RequestBody @Valid List<String> ids)
      throws NotAllowedException, NotFoundException, EntityInUseException, BadRequestException {
    if (isAdmin()) {
      for (String id : ids) projectManagement.delete(projectManagement.query(id));
    } else {
      throw new NotAllowedException("Forbidden to delete projects " + ids);
    }
  }

  /**
   * Returns the list of the Projects available
   *
   * @return List<Project>: The list of Projects available
   */
  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody Iterable<Project> findAll() throws NotFoundException, NotAllowedException {
    log.trace("Finding all Projects");
    Set<Project> projects = new HashSet<>();
    if (isAdmin()) {
      for (Project project : projectManagement.query()) projects.add(project);
    } else {
      for (Project project : projectManagement.query(getCurrentUser())) projects.add(project);
    }
    return projects;
  }

  /**
   * Returns the Project selected by id
   *
   * @param id : The id of the Project
   * @return Project: The Project selected
   */
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public Project findById(@PathVariable("id") String id)
      throws NotFoundException, NotAllowedException {
    log.trace("Finding Project with id " + id);
    Project project = projectManagement.query(id);
    if (project == null) throw new NotFoundException("Not found project " + id);
    log.trace("Found Project: " + project);
    if (isAdmin()) {
      return project;
    } else {
      for (Role role : getCurrentUser().getRoles()) {
        if (role.getProject().equals(project.getName())) {
          return project;
        }
      }
    }
    throw new NotAllowedException("Forbidden to access project " + id);
  }

  /**
   * Updates the Project
   *
   * @param new_project : The Project to be updated
   * @return Project The Project updated
   */
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Project update(@RequestBody @Valid Project new_project)
      throws NotFoundException, NotAllowedException {
    if (isAdmin()) {
      return projectManagement.update(new_project);
    } else {
      throw new NotAllowedException("Forbidden to update project " + new_project.getName());
    }
  }

  private User getCurrentUser() throws NotFoundException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) throw new NotFoundException("authentication invalid");
    String currentUserName = authentication.getName();
    return userManagement.queryByName(currentUserName);
  }

  public boolean isAdmin() throws NotAllowedException, NotFoundException {
    User currentUser = getCurrentUser();
    log.trace("Check user if admin: " + currentUser.getUsername());
    for (Role role : currentUser.getRoles()) {
      if (role.getRole().ordinal() == Role.RoleEnum.ADMIN.ordinal()) {
        return true;
      }
    }
    return false;
  }
}
