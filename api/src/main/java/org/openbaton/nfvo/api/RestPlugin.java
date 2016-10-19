package org.openbaton.nfvo.api;

import org.openbaton.nfvo.core.interfaces.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by rvl on 19.10.16.
 */
@RestController
@RequestMapping("/api/v1/plugins")
public class RestPlugin {

  @Autowired PluginManager pluginManager;

  @RequestMapping(value = "{type}/{name}/{version:.+}", method = RequestMethod.GET)
  public void downloadPlugin(
      @PathVariable String type, @PathVariable String name, @PathVariable String version)
      throws IOException {

    pluginManager.downloadPlugin(type, name, version);
  }
}
