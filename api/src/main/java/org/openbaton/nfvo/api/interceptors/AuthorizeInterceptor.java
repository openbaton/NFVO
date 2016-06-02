package org.openbaton.nfvo.api.interceptors;

import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
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

    @Autowired
    private UserManagement userManagement;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String projectId = request.getHeader("project-id");
        log.debug("ProjectId: " + projectId);

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
        }else {
            log.warn("AnonymousUser requesting a method: " + request.getMethod());
            return true;
        }
    }

    private boolean checkAuthorization(String project, HttpServletRequest request, String currentUserName) {

        log.trace("Current User: " + currentUserName);
        log.trace("UserManagement: " + userManagement);
        User user = userManagement.queryDB(currentUserName);

        if (user.getRoles().iterator().next().getRole().ordinal() == Role.RoleEnum.OB_ADMIN.ordinal())
            return true;

        if (user.getRoles().iterator().next().getRole().ordinal() == Role.RoleEnum.GUEST.ordinal())
            if (request.getMethod().equalsIgnoreCase("get"))
                return true;
            else
                return false;

        if (project != null) {
            for (Role role : user.getRoles())
                if (role.getProject().equals(project))
                    return true;

            throw new UnauthorizedUserException(currentUserName + " user is not unauthorized for executing this request!");
        }
        return true;
    }
}
