package org.openbaton.nfvo.api.interceptors;

import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.Role.RoleEnum;
import org.openbaton.catalogue.security.User;
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
        log.trace("Current User: " + currentUserName);

        if (currentUserName.equals("anonymousUser")) {
          if (request.getMethod().equalsIgnoreCase("get")) {
            return true;
          } else {
            log.warn("AnonymousUser requesting a method: " + request.getMethod());
            return true;
          }
        } else {
          return checkAuthorization(projectId, request, currentUserName);
        }
      } else /*if (request.getMethod().equalsIgnoreCase("get"))*/ {
        log.trace("AnonymousUser requesting a method: " + request.getMethod());
        return true;
      }
    } else {
      log.warn("AnonymousUser requesting a method: " + request.getMethod());
      return true;
    }
  }

  private boolean checkAuthorization(
      String project, HttpServletRequest request, String currentUserName) throws NotFoundException {

    log.trace("Current User: " + currentUserName);
    log.trace("projectId: " + project);
    log.trace(request.getMethod() + " URI: " + request.getRequestURI());

    if ((request.getRequestURI().equals("/api/v1/projects/")
            || (request.getRequestURI().equals("/api/v1/projects")))
        && request.getMethod().equalsIgnoreCase("get")) {
      return true;
    }
    log.trace(request.getMethod() + " URL: " + request.getRequestURL());
    log.trace("UserManagement: " + userManagement);
    User user = userManagement.queryDB(currentUserName);

    if (project != null) {
      if (!projectManagement.exist(project)) {
        throw new NotFoundException("Project with id " + project + " was not found");
      }
      if (user.getRoles().iterator().next().getRole().ordinal() == RoleEnum.OB_ADMIN.ordinal()) {
        log.trace("Return true for admin");
        return true;
      }

      if (user.getRoles().iterator().next().getRole().ordinal() == RoleEnum.GUEST.ordinal())
        if (request.getMethod().equalsIgnoreCase("get")) {
          log.trace("Return true for guest");
          return true;
        } else {
          log.trace("Return false for guest");
          return false;
        }

      for (Role role : user.getRoles()) {
        String pjName = projectManagement.query(project).getName();
        log.trace(role.getProject() + " == " + pjName);
        if (role.getProject().equals(pjName)) {
          log.trace("Return true");
          return true;
        }
      }

      throw new UnauthorizedUserException(
          currentUserName + " user is not unauthorized for executing this request!");
    }
    log.trace("Return false for project null");
    return false;
  }
}
