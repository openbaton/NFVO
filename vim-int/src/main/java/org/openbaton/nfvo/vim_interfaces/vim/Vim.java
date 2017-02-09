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

package org.openbaton.nfvo.vim_interfaces.vim;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.nfvo.vim_interfaces.flavor_management.DeploymentFlavorManagement;
import org.openbaton.nfvo.vim_interfaces.image_management.ImageManagement;
import org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement;
import org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement;
import org.openbaton.plugin.utils.RabbitPluginBroker;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by mpa on 12.06.15. */
@Service
@Scope("prototype")
@ConfigurationProperties
public abstract class Vim
    implements ImageManagement, ResourceManagement, NetworkManagement, DeploymentFlavorManagement {
  protected Logger log = LoggerFactory.getLogger(this.getClass());

  protected VimDriverCaller client;

  public Vim() {}

  public Vim(
      String type,
      String username,
      String password,
      String brokerIp,
      String managementPort,
      ApplicationContext context,
      String pluginName,
      int pluginTimeout,
      int port)
      throws PluginException {
    try {
      if (managementPort == null) {
        managementPort = "15672";
      }
      if (context == null) {
        client =
            new VimDriverCaller(
                brokerIp,
                username,
                password,
                port,
                type,
                pluginName,
                managementPort,
                pluginTimeout);
      } else {
        log.trace("Using context: " + context.getApplicationName());
        try {
          client =
              (VimDriverCaller)
                  ((RabbitPluginBroker) context.getBean("rabbitPluginBroker"))
                      .getVimDriverCaller(
                          brokerIp,
                          username,
                          password,
                          port,
                          type,
                          pluginName,
                          managementPort,
                          pluginTimeout);
        } catch (BeansException ignored) {
          client =
              new VimDriverCaller(
                  brokerIp,
                  username,
                  password,
                  port,
                  type,
                  pluginName,
                  managementPort,
                  pluginTimeout);
        }
      }
    } catch (TimeoutException | NotFoundException | IOException e) {
      throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
    }
  }

  public VimDriverCaller getClient() {
    return client;
  }

  public void setClient(VimDriverCaller client) {
    this.client = client;
  }
}
