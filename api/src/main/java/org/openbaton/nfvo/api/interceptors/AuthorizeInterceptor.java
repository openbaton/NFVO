package org.openbaton.nfvo.api.interceptors;

import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.Role.RoleEnum;
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
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
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
      } else /*if (request.getMethod().equalsIgnoreCase("get"))*/ {
        log.trace(
            "AnonymousUser requesting method: "
                + request.getMethod()
                + " on "
                + request.getRequestURI());
        if (request.getMethod().equalsIgnoreCase("get") && request.getRequestURI().equals("/")) {
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

  private boolean checkAuthorization(
      String project, HttpServletRequest request, String currentUserName)
      throws NotFoundException, NotAllowedException {

    log.trace("Current User: " + currentUserName);
    log.trace("projectId: " + project);
    log.trace(request.getMethod() + " URI: " + request.getRequestURI());
    log.trace("UserManagement: " + userManagement);
    User user = userManagement.queryByName(currentUserName);
    for (Role role : user.getRoles()) {
      if (role.getRole().ordinal() == Role.RoleEnum.ADMIN.ordinal()) {
        return true;
      }
    }
    if (project != null && !project.isEmpty()) {
      if (!projectManagement.exist(project)) {
        throw new NotFoundException("Project with id " + project + " was not found");
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
}
