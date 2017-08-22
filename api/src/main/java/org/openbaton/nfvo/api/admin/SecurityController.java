
package org.openbaton.nfvo.api.admin;

import java.security.Principal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SecurityController {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private SessionRegistry sessionRegistry;

  @RequestMapping(value = "/usernames", method = RequestMethod.GET)
  @ResponseBody
  public String currentUserName(Principal principal) {
    log.debug("User current is: " + principal.getName());

    List<Object> userSessions = sessionRegistry.getAllPrincipals();
    for (Object userSession : userSessions) {
      log.debug("user session of type: " + userSession.getClass().getCanonicalName());
    }

    return principal.getName();
  }
}
