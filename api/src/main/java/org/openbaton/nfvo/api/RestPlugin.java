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

package org.openbaton.nfvo.api;

import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.nfvo.core.interfaces.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

/**
 * Created by rvl on 19.10.16.
 */
@RestController
@RequestMapping("/api/v1/plugins")
public class RestPlugin {

  @Autowired PluginManager pluginManager;

  @RequestMapping(value = "{type}/{name}/{version:.+}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public void downloadPlugin(
      @PathVariable String type, @PathVariable String name, @PathVariable String version)
      throws IOException, AlreadyExistingException {

    pluginManager.downloadPlugin(type, name, version);
  }

  @RequestMapping(method = RequestMethod.GET)
  public @ResponseBody Set<String> getListVimDrivers() throws IOException {
    return pluginManager.listInstalledVimDrivers();
  }
}
