package org.openbaton.nfvo.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lto on 13/10/16.
 */
@RestController
@RequestMapping("/api/v1/version")
public class RestMain {

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  public String getVersion() {
    return RestMain.class.getPackage().getImplementationVersion();
  }
}
