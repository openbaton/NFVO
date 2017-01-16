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

package org.openbaton.vim.drivers;

import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import javax.annotation.PreDestroy;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.plugin.utils.PluginCaller;
import org.openbaton.vim.drivers.interfaces.VimDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 26/11/15. */
@Service
@Scope("prototype")
public class VimDriverCaller extends VimDriver {

  private PluginCaller pluginCaller;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  //  public VimDriverCaller(String type) throws IOException, TimeoutException, NotFoundException {
  //    pluginCaller =
  //        new PluginCaller(
  //            "vim-drivers." + type,
  //            "localhost",
  //            "admin",
  //            "openbaton",
  //            5672,
  //            15672,
  //            Long.parseLong(pluginTimeout));
  //  }
  //
  //  public VimDriverCaller(String name, String type)
  //      throws IOException, TimeoutException, NotFoundException {
  //    pluginCaller =
  //        new PluginCaller(
  //            "vim-drivers." + type + "." + name,
  //            "localhost",
  //            "admin",
  //            "openbaton",
  //            5672,
  //            15672,
  //            Long.parseLong(pluginTimeout));
  //  }

  //  public VimDriverCaller(String name, String type, String managementPort)
  //      throws IOException, TimeoutException, NotFoundException {
  //    if (name != null && !name.equals("")) {
  //      pluginCaller =
  //          new PluginCaller(
  //              "vim-drivers." + type + "." + name,
  //              "localhost",
  //              "admin",
  //              "openbaton",
  //              5672,
  //              Integer.parseInt(managementPort),
  //              Long.parseLong(pluginTimeout));
  //    } else {
  //      pluginCaller =
  //          new PluginCaller(
  //              "vim-drivers." + type,
  //              "localhost",
  //              "admin",
  //              "openbaton",
  //              5672,
  //              Integer.parseInt(managementPort),
  //              Long.parseLong(pluginTimeout));
  //    }
  //  }

  //  public VimDriverCaller(
  //      String brokerIp,
  //      String username,
  //      String password,
  //      int port,
  //      String type,
  //      String managementPort)
  //      throws IOException, TimeoutException, NotFoundException {
  //    pluginCaller =
  //        new PluginCaller(
  //            "vim-drivers." + type,
  //            brokerIp,
  //            username,
  //            password,
  //            port,
  //            Integer.parseInt(managementPort),
  //            Long.parseLong(pluginTimeout));
  //  }

  public VimDriverCaller(
      String brokerIp,
      String username,
      String password,
      int port,
      String type,
      String name,
      String managementPort,
      int pluginTimeout)
      throws IOException, TimeoutException, NotFoundException {
    log.trace("Creating PluginCaller");
    if (name == null) {
      name = "";
    }
    pluginCaller =
        new PluginCaller(
            "vim-drivers." + type + "." + name,
            brokerIp,
            username,
            password,
            port,
            Integer.parseInt(managementPort),
            pluginTimeout);
  }
  //
  //  public VimDriverCaller(
  //      String brokerIp, String username, String password, String type, String managementPort)
  //      throws IOException, TimeoutException, NotFoundException {
  //    pluginCaller =
  //        new PluginCaller(
  //            "vim-drivers." + type,
  //            brokerIp,
  //            username,
  //            password,
  //            5672,
  //            Integer.parseInt(managementPort),
  //            Long.parseLong(pluginTimeout));
  //  }

  @PreDestroy
  public void stop() throws IOException, TimeoutException {
    if (pluginCaller != null) {
      pluginCaller.close();
    }
  }

  @Override
  public Server launchInstance(
      VimInstance vimInstance,
      String name,
      String image,
      String flavor,
      String keypair,
      Set<String> network,
      Set<String> secGroup,
      String userData)
      throws VimDriverException {
    List<Serializable> params = new ArrayList<>();
    params.add(vimInstance);
    params.add(name);
    params.add(image);
    params.add(flavor);
    params.add(keypair);
    params.add((Serializable) network);
    params.add((Serializable) secGroup);
    params.add(userData);
    Serializable res = null;
    try {
      res = pluginCaller.executeRPC("launchInstance", params, Server.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (Server) res;
  }

  @Override
  public List<NFVImage> listImages(VimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    Type listType = new TypeToken<ArrayList<NFVImage>>() {}.getType();
    try {
      res = pluginCaller.executeRPC("listImages", params, listType);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (List<NFVImage>) res;
  }

  @Override
  public List<Server> listServer(VimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    try {
      Type listType = new TypeToken<ArrayList<Server>>() {}.getType();
      res = pluginCaller.executeRPC("listServer", params, listType);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (List<Server>) res;
  }

  @Override
  public List<Network> listNetworks(VimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    try {
      Type listType = new TypeToken<ArrayList<Network>>() {}.getType();
      res = pluginCaller.executeRPC("listNetworks", params, listType);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (List<Network>) res;
  }

  @Override
  public List<DeploymentFlavour> listFlavors(VimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    try {
      Type listType = new TypeToken<ArrayList<DeploymentFlavour>>() {}.getType();
      res = pluginCaller.executeRPC("listFlavors", params, listType);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (List<DeploymentFlavour>) res;
  }

  @Override
  public Server launchInstanceAndWait(
      VimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<String> networks,
      Set<String> securityGroups,
      String s,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(hostname);
    params.add(image);
    params.add(extId);
    params.add(keyPair);
    params.add((Serializable) networks);
    params.add((Serializable) securityGroups);
    params.add(s);
    params.add((Serializable) floatingIps);
    params.add((Serializable) keys);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("launchInstanceAndWait", params, Server.class);
    } catch (IOException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    } catch (PluginException e) {
      throw new VimDriverException(e.getMessage(), e.getCause());
    }
    return (Server) res;
  }

  @Override
  public Server launchInstanceAndWait(
      VimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<String> networks,
      Set<String> securityGroups,
      String s)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(hostname);
    params.add(image);
    params.add(extId);
    params.add(keyPair);
    params.add((Serializable) networks);
    params.add((Serializable) securityGroups);
    params.add(s);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("launchInstanceAndWait", params, Server.class);
    } catch (IOException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    } catch (PluginException e) {
      throw new VimDriverException(e.getMessage(), e.getCause());
    }
    return (Server) res;
  }

  @Override
  public void deleteServerByIdAndWait(VimInstance vimInstance, String id)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(id);
    try {
      pluginCaller.executeRPC("deleteServerByIdAndWait", params, null);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
  }

  @Override
  public Network createNetwork(VimInstance vimInstance, Network network) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(network);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("createNetwork", params, Network.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }

    return (Network) res;
  }

  @Override
  public DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(deploymentFlavour);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("addFlavor", params, DeploymentFlavour.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (DeploymentFlavour) res;
  }

  @Override
  public NFVImage addImage(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(image);
    params.add(imageFile);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("addImage", params, NFVImage.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (NFVImage) res;
  }

  @Override
  public NFVImage addImage(VimInstance vimInstance, NFVImage image, String image_url)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(image);
    params.add(image_url);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("addImage", params, NFVImage.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (NFVImage) res;
  }

  @Override
  public NFVImage updateImage(VimInstance vimInstance, NFVImage image) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(image);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("updateImage", params, NFVImage.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (NFVImage) res;
  }

  @Override
  public NFVImage copyImage(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(image);
    params.add(imageFile);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("copyImage", params, NFVImage.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (NFVImage) res;
  }

  @Override
  public boolean deleteImage(VimInstance vimInstance, NFVImage image) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(image);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("deleteImage", params, Boolean.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (boolean) res;
  }

  @Override
  public DeploymentFlavour updateFlavor(
      VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(deploymentFlavour);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("updateFlavor", params, DeploymentFlavour.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (DeploymentFlavour) res;
  }

  @Override
  public boolean deleteFlavor(VimInstance vimInstance, String extId) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(extId);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("deleteFlavor", params, Boolean.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (boolean) res;
  }

  @Override
  public Network updateNetwork(VimInstance vimInstance, Network network) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(network);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("updateNetwork", params, Network.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }

    return (Network) res;
  }

  @Override
  public Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(createdNetwork);
    params.add(subnet);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("createSubnet", params, Subnet.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (Subnet) res;
  }

  @Override
  public Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(updatedNetwork);
    params.add(subnet);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("updateSubnet", params, Subnet.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (Subnet) res;
  }

  @Override
  public List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(network_extId);
    Serializable res;
    try {
      Type listType = new TypeToken<ArrayList<String>>() {}.getType();
      res = pluginCaller.executeRPC("getSubnetsExtIds", params, listType);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (List<String>) res;
  }

  @Override
  public boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(existingSubnetExtId);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("deleteSubnet", params, Boolean.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (boolean) res;
  }

  @Override
  public boolean deleteNetwork(VimInstance vimInstance, String extId) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(extId);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("deleteNetwork", params, Boolean.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (boolean) res;
  }

  @Override
  public Network getNetworkById(VimInstance vimInstance, String id) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(id);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("getNetworkById", params, Network.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }

    return (Network) res;
  }

  @Override
  public Quota getQuota(VimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("getQuota", params, Quota.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (Quota) res;
  }

  @Override
  public String getType(VimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("getType", params, String.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }

    return (String) res;
  }
}
