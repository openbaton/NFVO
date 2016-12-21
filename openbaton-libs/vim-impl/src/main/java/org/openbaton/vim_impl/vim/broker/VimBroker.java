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

package org.openbaton.vim_impl.vim.broker;

import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.vim.drivers.interfaces.ClientInterfaces;
import org.openbaton.vim_impl.vim.AmazonVIM;
import org.openbaton.vim_impl.vim.GenericVIM;
import org.openbaton.vim_impl.vim.OpenstackVIM;
import org.openbaton.vim_impl.vim.TestVIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 20/05/15. */
@Service
@Scope
@ConfigurationProperties
public class VimBroker implements org.openbaton.nfvo.vim_interfaces.vim.VimBroker {

  @Value("${nfvo.rabbit.management.port:15672}")
  private String managementPort;

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String brokerIp;

  @Value("${nfvo.vim.drivers.allowInfiniteQuota:false}")
  private String allowInfiniteQuota;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ConfigurableApplicationContext context;

  private HashMap<String, ClientInterfaces> clientInterfaces;

  public String getAllowInfiniteQuota() {
    return allowInfiniteQuota;
  }

  public void setAllowInfiniteQuota(String allowInfiniteQuota) {
    this.allowInfiniteQuota = allowInfiniteQuota;
  }

  public String getManagementPort() {
    return managementPort;
  }

  public void setManagementPort(String managementPort) {
    this.managementPort = managementPort;
  }

  @PostConstruct
  private void init() {
    this.clientInterfaces = new HashMap<>();
  }

  @Override
  public void addClient(ClientInterfaces client, String type) {
    log.info("Registered client of type: " + type);
    this.clientInterfaces.put(type, client);
  }

  @Override
  public ClientInterfaces getClient(String type) {
    return this.clientInterfaces.get(type);
  }

  @Deprecated
  @Override
  public Vim getVim(String type, String name) throws PluginException {
    if (type.contains(".")) {
      type = type.split("\\.")[0];
    }
    switch (type) {
      case "test":
        return (Vim) context.getBean("testVIM", type, name, this.managementPort);
      case "openstack":
        return (Vim) context.getBean("openstackVIM", type, name, this.managementPort, context);
      case "amazon":
        return (Vim) context.getBean("amazonVIM", type, name, this.managementPort);
      default:
        return new GenericVIM(name, type, context);
    }
  }

  @Override
  public Vim getVim(String type) throws PluginException {
    String name = null;
    if (type.contains(".")) {
      String[] split = type.split("\\.");
      type = split[0];
      name = split[1];
    }
    switch (type) {
      case "test":
        //                return (Vim) context.getBean("testVIM", this.port);
        if (name != null) return new TestVIM(name, 5672, this.managementPort);
        return new TestVIM(this.managementPort);
      case "openstack":
        //                return (Vim) context.getBean("openstackVIM", this.port, context);
        if (name != null)
          return new OpenstackVIM(name, 5672, this.managementPort, context, brokerIp);
        return new OpenstackVIM(this.managementPort, context);

      case "amazon":
        //                return (Vim) context.getBean("amazonVIM", this.port);
        return new AmazonVIM(this.managementPort);
      default:
        if (name != null)
          return new GenericVIM(type + "." + name, brokerIp, this.managementPort, context);
        return new GenericVIM(type, context);
    }
  }

  @Override
  public Vim getVim(String type, int port) throws PluginException {
    String name = null;
    if (type.contains(".")) {
      String[] split = type.split("\\.");
      type = split[0];
      name = split[1];
    }
    switch (type) {
      case "test":
        return (Vim) context.getBean("testVIM", port, this.managementPort);
      case "openstack":
        return (Vim) context.getBean("openstackVIM", port, this.managementPort, context);
      case "amazon":
        return (Vim) context.getBean("amazonVIM", port, this.managementPort);
      default:
        return new GenericVIM(type + "." + name, this.brokerIp, port, this.managementPort, context);
    }
  }

  @Override
  public Vim getVim(String type, String name, String port) {
    if (type.contains(".")) type = type.split("\\.")[0];
    switch (type) {
      case "test":
        return (Vim)
            context.getBean("testVIM", type, name, Integer.parseInt(port), this.managementPort);
      case "openstack":
        return (Vim)
            context.getBean(
                "openstackVIM", type, name, Integer.parseInt(port), this.managementPort, context);
      case "amazon":
        return (Vim)
            context.getBean("amazonVIM", type, name, Integer.parseInt(port), this.managementPort);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Quota getLeftQuota(VimInstance vimInstance) throws VimException, PluginException {
    Vim vim = getVim(vimInstance.getType());

    Quota maximalQuota = vim.getQuota(vimInstance);

    if (allowInfiniteQuota != null && allowInfiniteQuota.equalsIgnoreCase("true")) {
      if (maximalQuota.getInstances() == -1) {
        maximalQuota.setInstances(Integer.MAX_VALUE);
      }
      if (maximalQuota.getRam() == -1) {
        maximalQuota.setRam(Integer.MAX_VALUE);
      }
      if (maximalQuota.getCores() == -1) {
        maximalQuota.setCores(Integer.MAX_VALUE);
      }
      if (maximalQuota.getKeyPairs() == -1) {
        maximalQuota.setKeyPairs(Integer.MAX_VALUE);
      }
      if (maximalQuota.getFloatingIps() == -1) {
        maximalQuota.setFloatingIps(Integer.MAX_VALUE);
      }
    } else {
      if (maximalQuota.getInstances() < 0
          || maximalQuota.getRam() < 0
          || maximalQuota.getCores() < 0
          || maximalQuota.getKeyPairs() < 0
          || maximalQuota.getFloatingIps() < 0) {
        log.error(
            "Infinite quota are not allowed. Please set nfvo.vim.drivers.allowInfiniteQuota to true or change the quota in your VIM installation");
        throw new VimException(
            "Infinite quota are not allowed. Please set nfvo.vim.drivers.allowInfiniteQuota to true or change the quota in your VIM installation");
      }
    }

    List<Server> servers = vim.queryResources(vimInstance);
    //Calculate used resource by servers (cpus, ram)
    for (Server server : servers) {
      //Subtract floatingIps

      //Subtract instances
      maximalQuota.setInstances(maximalQuota.getInstances() - 1);
      //Subtract used ram and cpus
      // TODO check whenever the library/rest command work.
      DeploymentFlavour flavor = server.getFlavor();
      maximalQuota.setRam(maximalQuota.getRam() - flavor.getRam());
      maximalQuota.setCores(maximalQuota.getCores() - flavor.getVcpus());
      // TODO add floating ips when quota command will work...
    }
    return maximalQuota;
  }
}
