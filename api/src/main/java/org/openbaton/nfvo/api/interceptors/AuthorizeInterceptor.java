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

package org.openbaton.nfvo.api.interceptors;

import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.security.interfaces.ProjectManagement;
import org.openbaton.nfvo.security.interfaces.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lto on 25/05/16.
 */
@Service
public class AuthorizeInterceptor extends HandlerInterceptorAdapter {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private UserManagement userManagement;
  @Autowired private ProjectManagement projectManagement;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    String projectId = request.getHeader("project-id");
    log.trace("ProjectId: " + projectId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    log.trace("Authentication " + authentication);
    if (authentication != null) {
      if (!(authentication instanceof AnonymousAuthenticationToken)) {
        String currentUserName = authentication.getName();
        return checkAuthorization(projectId, request, currentUserName);
      } else {
        log.trace(
            "AnonymousUser requesting method: "
                + request.getMethod()
                + " on "
                + request.getRequestURI());
        if (isLogin(request)) {
          return true;
        } else if (alwaysAllowedPath(request)) {
          return true;
        } else {
          return false;
        }
      }
    } else {
      log.warn(
          "AnonymousUser requesting method: "
              + request.getMethod()
              + " on "
              + request.getRequestURI());
      return false;
    }
  }

  private boolean isLogin(HttpServletRequest request) {
    return request.getMethod().equalsIgnoreCase("get") && request.getRequestURI().equals("/");
  }

  private boolean checkAuthorization(
      String project, HttpServletRequest request, String currentUserName)
      throws NotFoundException, NotAllowedException {

    log.trace("Current User: " + currentUserName);
    log.trace("projectId: \"" + project + "\"");
    log.trace(request.getMethod() + " on URI: " + request.getRequestURI());
    log.trace("UserManagement: " + userManagement);
    User user = userManagement.queryByName(currentUserName);

    if (project != null && !project.isEmpty()) {

      if (projectIsNecessary(request) && !projectManagement.exist(project)) {
        throw new NotFoundException("Project with id " + project + " was not found");
      }
      for (Role role : user.getRoles()) {
        if (role.getRole().ordinal() == Role.RoleEnum.ADMIN.ordinal()) {
          return true;
        }
      }
      for (Role role : user.getRoles()) {
        String pjName = projectManagement.query(project).getName();
        log.trace(role.getProject() + " == " + pjName);
        if (role.getProject().equals(pjName)) {
          if (role.getRole().ordinal() == Role.RoleEnum.GUEST.ordinal()
              && !request.getMethod().equalsIgnoreCase("get")) {
            throw new NotAllowedException("Guest is only allowed to execute GET");
          } else {
            log.trace("Return true");
            return true;
          }
        }
      }
    } else {
      Iterable<Project> userProjects = projectManagement.query(user);
      if (userProjects.iterator().hasNext()) {
        return checkAuthorization(userProjects.iterator().next().getId(), request, currentUserName);
      } else {
        throw new NotFoundException(
            "Not Found any project you are assigned to. Please ask an admin to assign a project to you.");
      }
    }
    return false;
  }

  //TODO realize this configurable
  private boolean alwaysAllowedPath(HttpServletRequest request) {
    return (request.getMethod().equalsIgnoreCase("post")
            && request.getRequestURI().equals("/admin/v1/vnfm-register"))
        || (request.getMethod().equalsIgnoreCase("post")
            && request.getRequestURI().equals("/admin/v1/vnfm-unregister"))
        || (request.getMethod().equalsIgnoreCase("post")
            && request.getRequestURI().startsWith("/admin/v1/vnfm-core-"));
  }

  //TODO realize this configurable
  private boolean projectIsNecessary(HttpServletRequest request) {
    return !((request.getRequestURI().equals("/api/v1/projects"))
        || (request.getRequestURI().equals("/api/v1/projects/"))
        || (request.getRequestURI().equals("/api/v1/users/current"))
        || (request.getRequestURI().equals("/api/v1/users"))
        || (request.getRequestURI().equals("/api/v1/version"))
        || (request.getRequestURI().equals("/api/v1/users/")));
  }
}
