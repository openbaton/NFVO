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
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.networks.Subnet;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
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

@Service
@Scope("prototype")
@SuppressWarnings({"unsafe", "unchecked"})
public class VimDriverCaller extends VimDriver {

  private PluginCaller pluginCaller;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  public VimDriverCaller(
      String brokerIp,
      String username,
      String password,
      int port,
      String virtualHost,
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
            virtualHost,
            Integer.parseInt(managementPort),
            pluginTimeout);
  }

  @PreDestroy
  public void stop() throws IOException, TimeoutException {
    if (pluginCaller != null) pluginCaller.close();
  }

  @Override
  public Server launchInstance(
      BaseVimInstance vimInstance,
      String name,
      String image,
      String flavor,
      String keypair,
      Set<VNFDConnectionPoint> networks,
      Set<String> secGroup,
      String userData)
      throws VimDriverException {
    List<Serializable> params = new ArrayList<>();
    params.add(vimInstance);
    params.add(name);
    params.add(image);
    params.add(flavor);
    params.add(keypair);
    params.add((Serializable) networks);
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
  public List<BaseNfvImage> listImages(BaseVimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    Type listType = new TypeToken<ArrayList<BaseNfvImage>>() {}.getType();
    try {
      res = pluginCaller.executeRPC("listImages", params, listType);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (List<BaseNfvImage>) res;
  }

  @Override
  public List<Server> listServer(BaseVimInstance vimInstance) throws VimDriverException {
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
  public List<BaseNetwork> listNetworks(BaseVimInstance vimInstance) throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    Serializable res;
    try {
      Type listType = new TypeToken<ArrayList<BaseNetwork>>() {}.getType();
      res = pluginCaller.executeRPC("listNetworks", params, listType);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (List<BaseNetwork>) res;
  }

  @Override
  public List<DeploymentFlavour> listFlavors(BaseVimInstance vimInstance)
      throws VimDriverException {
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
      BaseVimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<VNFDConnectionPoint> networks,
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
      BaseVimInstance vimInstance,
      String hostname,
      String image,
      String extId,
      String keyPair,
      Set<VNFDConnectionPoint> networks,
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
  public void deleteServerByIdAndWait(BaseVimInstance vimInstance, String id)
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
  public Network createNetwork(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimDriverException {
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
  public DeploymentFlavour addFlavor(
      BaseVimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimDriverException {
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
  public NFVImage addImage(BaseVimInstance vimInstance, BaseNfvImage image, byte[] imageFile)
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
  public NFVImage addImage(BaseVimInstance vimInstance, BaseNfvImage image, String image_url)
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
  public BaseNfvImage updateImage(BaseVimInstance vimInstance, BaseNfvImage image)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(image);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("updateImage", params, NFVImage.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }
    return (BaseNfvImage) res;
  }

  @Override
  public BaseNfvImage copyImage(BaseVimInstance vimInstance, BaseNfvImage image, byte[] imageFile)
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
    return (BaseNfvImage) res;
  }

  @Override
  public boolean deleteImage(BaseVimInstance vimInstance, BaseNfvImage image)
      throws VimDriverException {
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
      BaseVimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimDriverException {
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
  public boolean deleteFlavor(BaseVimInstance vimInstance, String extId) throws VimDriverException {
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
  public Network updateNetwork(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimDriverException {
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
  public Subnet createSubnet(BaseVimInstance vimInstance, BaseNetwork createdNetwork, Subnet subnet)
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
  public Subnet updateSubnet(BaseVimInstance vimInstance, BaseNetwork updatedNetwork, Subnet subnet)
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
  public List<String> getSubnetsExtIds(BaseVimInstance vimInstance, String network_extId)
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
  public boolean deleteSubnet(BaseVimInstance vimInstance, String existingSubnetExtId)
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
  public boolean deleteNetwork(BaseVimInstance vimInstance, String extId)
      throws VimDriverException {
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
  public BaseNetwork getNetworkById(BaseVimInstance vimInstance, String id)
      throws VimDriverException {
    List<Serializable> params = new LinkedList<>();
    params.add(vimInstance);
    params.add(id);
    Serializable res;
    try {
      res = pluginCaller.executeRPC("getNetworkById", params, Network.class);
    } catch (IOException | PluginException | InterruptedException e) {
      throw new VimDriverException(e.getMessage());
    }

    return (BaseNetwork) res;
  }

  @Override
  public Quota getQuota(BaseVimInstance vimInstance) throws VimDriverException {
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
  public String getType(BaseVimInstance vimInstance) throws VimDriverException {
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
