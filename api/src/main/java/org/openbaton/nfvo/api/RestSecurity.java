package org.openbaton.nfvo.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lto on 19/10/15.
 */
@RestController
@RequestMapping("/api/v1/security")
@ConfigurationProperties(prefix = "nfvo.security")
public class RestSecurity {
  private boolean enabled;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseStatus(HttpStatus.OK)
  private String isSecurityEnabled() {
    log.debug("is Security enabled? " + enabled);
    return String.valueOf(enabled).toLowerCase();
  }
}
