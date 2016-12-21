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

package org.openbaton.vim_impl.vim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/** Created by lto on 06/04/16. */
@Service
@Scope(value = "prototype")
public class GenericVIM extends Vim {

  public GenericVIM(String type, String name, ApplicationContext context) throws PluginException {
    super(type, name, context);
  }

  public GenericVIM(
      String type,
      String username,
      String password,
      String brokerIp,
      String managementPort,
      ApplicationContext context)
      throws PluginException {
    super(type, username, password, brokerIp, managementPort, context);
  }

  public GenericVIM(
      String type, String brokerIp, int port, String managementPort, ApplicationContext context)
      throws PluginException {
    super(type, brokerIp, port, managementPort, context);
  }

  public GenericVIM() {}

  public GenericVIM(String type, String name, String managementPort, ApplicationContext context)
      throws PluginException {
    super(type, name, managementPort, context);
  }

  public GenericVIM(String type, ConfigurableApplicationContext context) throws PluginException {
    super(type, context);
  }

  @Override
  public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException {
    try {
      log.debug(
          "Adding DeploymentFlavour with name "
              + deploymentFlavour.getFlavour_key()
              + " to VimInstance "
              + vimInstance.getName());
      DeploymentFlavour flavor = client.addFlavor(vimInstance, deploymentFlavour);
      log.info(
          "Added Flavor with name: "
              + deploymentFlavour.getFlavour_key()
              + " to VimInstance "
              + vimInstance.getName());
      return flavor;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not added Flavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not added Flavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not added Image with name: "
              + deploymentFlavour.getFlavour_key()
              + " successfully to VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException {
    boolean isDeleted = false;
    try {
      log.debug(
          "Deleting DeploymentFlavor with name "
              + deploymentFlavour.getFlavour_key()
              + " from VimInstance "
              + vimInstance.getName());
      isDeleted = client.deleteFlavor(vimInstance, deploymentFlavour.getExtId());
      if (isDeleted) {
        log.info(
            "Deleted DeploymentFlavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " from VimInstance "
                + vimInstance.getName());
      } else {
        log.error(
            "Not deleted DeploymentFlavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " successfully from VimInstance "
                + vimInstance.getName());
        throw new VimException(
            "Not deleted Flavor with id: "
                + deploymentFlavour.getFlavour_key()
                + " successfully from VimInstance "
                + vimInstance.getName());
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not deleted DeploymentFlavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " successfully from VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not deleted DeploymentFlavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " successfully from VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not deleted DeploymentFlavor with name: "
              + deploymentFlavour.getFlavour_key()
              + " successfully from VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException {
    try {
      log.debug(
          "Updating DeploymentFlavour with name "
              + deploymentFlavour.getFlavour_key()
              + " on VimInstance "
              + vimInstance.getName());
      DeploymentFlavour flavor = client.updateFlavor(vimInstance, deploymentFlavour);
      log.info(
          "Updated Flavor with name: "
              + deploymentFlavour.getId()
              + " on VimInstance "
              + vimInstance.getName());
      return flavor;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not updated Flavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not updated Flavor with name: "
                + deploymentFlavour.getFlavour_key()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not updated Flavor with name: "
              + deploymentFlavour.getFlavour_key()
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance)
      throws VimException {
    try {
      log.debug("Listing DeploymentFlavors of VimInstance " + vimInstance.getName());
      List<DeploymentFlavour> flavors = client.listFlavors(vimInstance);
      log.info("Listed DeploymentFlavors of VimInstance " + vimInstance.getName());
      for (DeploymentFlavour flavour : flavors) log.debug("\t" + flavour.getFlavour_key());
      return flavors;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not listed DeploymentFlavors successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not listed DeploymentFlavors successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not listed DeploymentFlavors successfully of VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public NFVImage add(VimInstance vimInstance, NFVImage image, byte[] imageFile)
      throws VimException {
    try {
      log.debug(
          "Adding image with name: "
              + image.getName()
              + " to VimInstance "
              + vimInstance.getName()
              + " using passed image file");
      NFVImage addedImage = client.addImage(vimInstance, image, imageFile);
      log.info(
          "Added Image with name: " + image.getName() + " to VimInstance " + vimInstance.getName());
      return addedImage;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not added Image with name: "
                + image.getName()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not added Image with name: "
                + image.getName()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not added Image with name: "
              + image.getName()
              + " successfully to VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public NFVImage add(VimInstance vimInstance, NFVImage image, String image_url)
      throws VimException {
    try {
      log.debug(
          "Adding image with name: "
              + image.getName()
              + " to VimInstance "
              + vimInstance.getName()
              + " using image_url: "
              + image_url);
      NFVImage addedImage = client.addImage(vimInstance, image, image_url);
      log.info(
          "Added Image with name: " + image.getName() + " to VimInstance " + vimInstance.getName());
      return addedImage;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not added Image with name: "
                + image.getName()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not added Image with name: "
                + image.getName()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not added Image with name: "
              + image.getName()
              + " successfully to VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public void delete(VimInstance vimInstance, NFVImage image) throws VimException {
    boolean isDeleted = false;
    try {
      log.debug(
          "Deleting image with name: "
              + image.getName()
              + " on VimInstance "
              + vimInstance.getName());
      isDeleted = client.deleteImage(vimInstance, image);
      if (isDeleted) {
        log.info(
            "Deleted Image with name: "
                + image.getName()
                + " on VimInstance "
                + vimInstance.getName());
      } else {
        log.warn(
            "Not deleted Image with name: "
                + image.getName()
                + " successfully on VimInstance "
                + vimInstance.getName());
        throw new VimException(
            "Not deleted Image with id: "
                + image.getId()
                + " successfully on VimInstance "
                + vimInstance.getName());
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not deleted Image with name: "
                + image.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not deleted Image with name: "
                + image.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not deleted Image with name: "
              + image.getName()
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException {
    try {
      log.debug(
          "Updating image with name: "
              + image.getName()
              + " on VimInstance "
              + vimInstance.getName());
      NFVImage updatedImage = client.updateImage(vimInstance, image);
      log.info(
          "Updated Image with name: "
              + image.getName()
              + " on VimInstance "
              + vimInstance.getName());
      return updatedImage;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not updated Image with name: "
                + image.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not updated Image with name: "
                + image.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not updated Image with name: "
              + image.getName()
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public List<NFVImage> queryImages(VimInstance vimInstance) throws VimException {
    log.debug("Listing all Images of VimInstance " + vimInstance.getName());
    try {
      log.trace("Client is: " + client);

      List<NFVImage> images = client.listImages(vimInstance);

      log.info("Listed Images of VimInstance " + vimInstance.getName());
      for (NFVImage image : images) log.debug("\t" + image.getName());
      return images;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not listed Images successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not listed Images successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not listed Images successfully of VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public void copy(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException {
    try {
      log.debug(
          "Copying image with name "
              + image.getName()
              + " to VimInstance "
              + vimInstance.getName()
              + " using image file");
      client.copyImage(vimInstance, image, imageFile);
      log.info(
          "Copied Image with name: "
              + image.getName()
              + " to VimInstance "
              + vimInstance.getName());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not copied Image with name: "
                + image.getName()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not copied Image with name: "
                + image.getName()
                + " successfully to VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not copied Image with name: "
              + image.getName()
              + " successfully to VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public Network add(VimInstance vimInstance, Network network) throws VimException {
    Network createdNetwork;
    try {
      log.debug(
          "Creating Network with name: "
              + network.getName()
              + " on VimInstance "
              + vimInstance.getName());
      createdNetwork = client.createNetwork(vimInstance, network);
      log.info(
          "Created Network with name: "
              + network.getName()
              + " on VimInstance "
              + vimInstance.getName());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not created Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not created Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not created Network with name: "
              + network.getName()
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
    log.debug(
        "Creating Subnets for Network with name: "
            + network.getName()
            + " on VimInstance "
            + vimInstance.getName()
            + " -> Subnets: "
            + network.getSubnets());
    Set<Subnet> createdSubnets = new HashSet<>();
    for (Subnet subnet : network.getSubnets()) {
      try {
        log.debug(
            "Creating Subnet with name: "
                + subnet.getName()
                + " on Network with name: "
                + network.getName()
                + " on VimInstance "
                + vimInstance.getName());
        Subnet createdSubnet = client.createSubnet(vimInstance, createdNetwork, subnet);
        log.info(
            "Created Subnet with name: "
                + subnet.getName()
                + " on Network with name: "
                + network.getName()
                + " on VimInstance "
                + vimInstance.getName());
        createdSubnet.setNetworkId(createdNetwork.getId());
        createdSubnets.add(createdSubnet);
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.error(
              "Not created Subnet with name: "
                  + subnet.getName()
                  + " successfully on Network with name: "
                  + network.getName()
                  + " on VimInstnace "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage(),
              e);
        } else {
          log.error(
              "Not created Subnet with name: "
                  + subnet.getName()
                  + " successfully on Network with name: "
                  + network.getName()
                  + " on VimInstnace "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage());
        }
        throw new VimException(
            "Not created Subnet with name: "
                + subnet.getName()
                + " successfully on Network with name: "
                + network.getName()
                + " on VimInstnace "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      }
    }
    createdNetwork.setSubnets(createdSubnets);
    log.info(
        "Created Subnets on Network with name: "
            + network.getName()
            + " on VimInstnace "
            + vimInstance.getName()
            + " -> Subnets: "
            + network.getSubnets());
    return createdNetwork;
  }

  @Override
  public void delete(VimInstance vimInstance, Network network) throws VimException {
    boolean isDeleted;
    try {
      log.debug(
          "Deleting Network with name: "
              + network.getName()
              + " on VimInstance "
              + vimInstance.getName());
      isDeleted = client.deleteNetwork(vimInstance, network.getExtId());
      if (isDeleted) {
        log.info(
            "Deleted Network with name: "
                + network.getName()
                + " on VimInstance "
                + vimInstance.getName());
      } else {
        log.error(
            "Not deleted Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName());
        throw new VimException(
            "Not deleted Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName());
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not deleted Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not deleted Network with name: "
                + network.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not deleted Network with name: "
              + network.getName()
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
    Network updatedNetwork = null;
    try {
      log.debug(
          "Updating Network with name: "
              + updatingNetwork.getName()
              + " on VimInstance "
              + vimInstance.getName());
      updatedNetwork = client.updateNetwork(vimInstance, updatingNetwork);
      log.info(
          "Updated Network with name: "
              + updatingNetwork.getName()
              + " on VimInstance "
              + vimInstance.getName());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not updated Network with name: "
                + updatingNetwork.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not updated Network with name: "
                + updatingNetwork.getName()
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not updated Network with name: "
              + updatingNetwork.getName()
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
    log.debug(
        "Updating Subnets for Network with name: "
            + updatingNetwork.getName()
            + " on VimInstance "
            + vimInstance.getName()
            + " -> "
            + updatingNetwork.getSubnets());
    Set<Subnet> updatedSubnets = new HashSet<>();
    List<String> updatedSubnetExtIds = new ArrayList<>();
    for (Subnet subnet : updatingNetwork.getSubnets()) {
      if (subnet.getExtId() != null) {
        try {
          log.debug(
              "Updating Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          Subnet updatedSubnet = client.updateSubnet(vimInstance, updatedNetwork, subnet);
          log.info(
              "Updated Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          updatedSubnet.setNetworkId(updatedNetwork.getId().toString());
          updatedSubnets.add(updatedSubnet);
          updatedSubnetExtIds.add(updatedSubnet.getExtId());
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.error(
                "Not updated Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + updatingNetwork.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage(),
                e);
          } else {
            log.error(
                "Not updated Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + updatingNetwork.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage());
          }
          throw new VimException(
              "Not updated Subnet with name: "
                  + subnet.getName()
                  + " successfully on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage(),
              e);
        }
      } else {
        try {
          log.debug(
              "Creating Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          Subnet createdSubnet = client.createSubnet(vimInstance, updatedNetwork, subnet);
          log.info(
              "Created Subnet with name: "
                  + subnet.getName()
                  + " on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          createdSubnet.setNetworkId(updatedNetwork.getId().toString());
          updatedSubnets.add(createdSubnet);
          updatedSubnetExtIds.add(createdSubnet.getExtId());
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.error(
                "Not created Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + updatingNetwork.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage(),
                e);
          } else {
            log.error(
                "Not created Subnet with name: "
                    + subnet.getName()
                    + " successfully on Network with name: "
                    + updatingNetwork.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage());
          }
          throw new VimException(
              "Not created Subnet with name: "
                  + subnet.getName()
                  + " successfully on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage(),
              e);
        }
      }
    }
    updatedNetwork.setSubnets(updatedSubnets);
    List<String> existingSubnetExtIds = null;
    try {
      log.debug(
          "Listing all Subnet IDs of Network with name: "
              + updatingNetwork.getName()
              + " on VimInstance "
              + vimInstance.getName());
      existingSubnetExtIds = client.getSubnetsExtIds(vimInstance, updatedNetwork.getExtId());
      log.info(
          "Listed all Subnet IDs of Network with name: "
              + updatingNetwork.getName()
              + " on VimInstance "
              + vimInstance.getName()
              + " -> Subnet IDs: "
              + existingSubnetExtIds);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not listed Subnets of Network with name: "
                + updatingNetwork.getName()
                + " successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not listed Subnets of Network with name: "
                + updatingNetwork.getName()
                + " successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not listed Subnets of Network with name: "
              + updatingNetwork.getName()
              + " successfully of VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
    for (String existingSubnetExtId : existingSubnetExtIds) {
      if (!updatedSubnetExtIds.contains(existingSubnetExtId)) {
        try {
          log.debug(
              "Deleting Subnet with id: "
                  + existingSubnetExtId
                  + " on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
          client.deleteSubnet(vimInstance, existingSubnetExtId);
          log.info(
              "Deleted Subnet with id: "
                  + existingSubnetExtId
                  + " on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName());
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.error(
                "Not Deleted Subnet with id: "
                    + existingSubnetExtId
                    + " successfully on Network with name: "
                    + updatingNetwork.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage(),
                e);
          } else {
            log.error(
                "Not Deleted Subnet with id: "
                    + existingSubnetExtId
                    + " successfully on Network with name: "
                    + updatingNetwork.getName()
                    + " on VimInstance "
                    + vimInstance.getName()
                    + ". Caused by: "
                    + e.getMessage());
          }
          throw new VimException(
              "Not Deleted Subnet with id: "
                  + existingSubnetExtId
                  + " successfully on Network with name: "
                  + updatingNetwork.getName()
                  + " on VimInstance "
                  + vimInstance.getName()
                  + ". Caused by: "
                  + e.getMessage(),
              e);
        }
      }
    }
    log.info(
        "Subnets of Network with name: "
            + updatingNetwork.getName()
            + " updated successfully on VimInstance "
            + vimInstance.getName());
    return updatedNetwork;
  }

  @Override
  public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
    try {
      log.debug("Listing all Networks of VimInstance " + vimInstance.getName());
      List<Network> networks = client.listNetworks(vimInstance);
      log.info("Listed Networks of VimInstance " + vimInstance.getName());
      for (Network network : networks) log.debug("\t" + network.getName());
      return networks;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not listed Networks successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not listed Networks successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not listed Networks successfully of VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public Network query(VimInstance vimInstance, String extId) throws VimException {
    try {
      log.debug(
          "Finding Network with extId: " + extId + " on VimInstance " + vimInstance.getName());
      Network network = client.getNetworkById(vimInstance, extId);
      log.info(
          "Found Network with extId: "
              + network.getId()
              + " on VimInstance "
              + vimInstance.getName()
              + " -> Network: "
              + network);
      return network;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not found Network with extId: "
                + extId
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not found Network with extId: "
                + extId
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not found Network with extId: "
              + extId
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  @Async
  public Future<VNFCInstance> allocate(
      VimInstance vimInstance,
      VirtualDeploymentUnit vdu,
      VirtualNetworkFunctionRecord vnfr,
      VNFComponent vnfComponent,
      String userdata,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimException {
    log.info("Launching new VM on VimInstance: " + vimInstance.getName());
    log.trace("VDU is : " + vdu.toString());
    log.trace("VNFR is : " + vnfr.toString());
    log.trace("VNFC is : " + vnfComponent.toString());
    /** *) choose image *) ...? */
    String image = this.chooseImage(vdu.getVm_image(), vimInstance);

    log.info("Finding Networks...");
    Set<String> networks = new HashSet<>();
    for (VNFDConnectionPoint vnfdConnectionPoint : vnfComponent.getConnection_point()) {
      for (Network net : vimInstance.getNetworks())
        if (vnfdConnectionPoint.getVirtual_link_reference().equals(net.getName()))
          networks.add(net.getExtId());
    }
    log.info("Found Networks with ExtIds: " + networks);

    String flavorExtId = getFlavorExtID(vnfr.getDeployment_flavour_key(), vimInstance);

    log.info("Generating Hostname...");
    vdu.setHostname(vnfr.getName());
    String hostname = vdu.getHostname() + "-" + ((int) (Math.random() * 1000));
    log.info("Generated Hostname: " + hostname);

    userdata = userdata.replace("#Hostname=", "Hostname=" + hostname);

    log.info("Using SecurityGroups: " + vimInstance.getSecurityGroups());

    log.info(
        "Launching VM with parameters: "
            + hostname
            + " - "
            + image
            + " - "
            + flavorExtId
            + " - "
            + vimInstance.getKeyPair()
            + " - "
            + networks
            + " - "
            + vimInstance.getSecurityGroups());

    Server server;

    if (vimInstance == null) throw new NullPointerException("VimInstance is null");
    if (hostname == null) throw new NullPointerException("hostname is null");
    if (image == null) throw new NullPointerException("image is null");
    if (flavorExtId == null) throw new NullPointerException("flavorExtId is null");
    if (vimInstance.getKeyPair() == null) {
      log.debug("vimInstance.getKeyPair() is null");
      vimInstance.setKeyPair("");
    }
    if (networks == null) throw new NullPointerException("networks is null");
    if (vimInstance.getSecurityGroups() == null)
      throw new NullPointerException("vimInstance.getSecurityGroups() is null");

    try {

      server =
          client.launchInstanceAndWait(
              vimInstance,
              hostname,
              image,
              flavorExtId,
              vimInstance.getKeyPair(),
              networks,
              vimInstance.getSecurityGroups(),
              userdata,
              floatingIps,
              new HashSet<>(keys));
      log.debug(
          "Launched VM with hostname "
              + hostname
              + " with ExtId "
              + server.getExtId()
              + " on VimInstance "
              + vimInstance.getName());

    } catch (VimDriverException e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not launched VM with hostname "
                + hostname
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not launched VM with hostname "
                + hostname
                + " successfully on VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      VNFCInstance vnfcInstance = null;
      VimDriverException vimDriverException = (VimDriverException) e.getCause();
      server = vimDriverException.getServer();

      if (server != null) {
        vnfcInstance =
            getVnfcInstanceFromServer(
                vimInstance, vnfComponent, hostname, server, vdu, floatingIps, vnfr);
      }

      throw new VimException(
          "Not launched VM with hostname "
              + hostname
              + " successfully on VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e,
          vdu,
          vnfcInstance);
    }

    log.debug("Creating VNFCInstance based on the VM launched previously -> VM: " + server);
    VNFCInstance vnfcInstance =
        getVnfcInstanceFromServer(
            vimInstance, vnfComponent, hostname, server, vdu, floatingIps, vnfr);

    log.info("Launched VNFCInstance: " + vnfcInstance + " on VimInstance " + vimInstance.getName());
    return new AsyncResult<>(vnfcInstance);
  }

  protected String getFlavorExtID(String key, VimInstance vimInstance) throws VimException {
    log.debug(
        "Finding DeploymentFlavor with name: " + key + " on VimInstance " + vimInstance.getName());
    for (DeploymentFlavour deploymentFlavour : vimInstance.getFlavours()) {
      if (deploymentFlavour.getFlavour_key().equals(key)
          || deploymentFlavour.getExtId().equals(key)
          || deploymentFlavour.getId().equals(key)) {
        log.info(
            "Found DeploymentFlavour with ExtId: "
                + deploymentFlavour.getExtId()
                + " of DeploymentFlavour with name: "
                + key
                + " on VimInstance "
                + vimInstance.getName());
        return deploymentFlavour.getExtId();
      }
    }
    log.error(
        "Not found DeploymentFlavour with name: "
            + key
            + " on VimInstance "
            + vimInstance.getName());
    throw new VimException(
        "Not found DeploymentFlavour with name: "
            + key
            + " on VimInstance "
            + vimInstance.getName());
  }

  protected String chooseImage(Collection<String> vm_images, VimInstance vimInstance)
      throws VimException {
    log.debug("Choosing Image...");
    log.debug("Requested: " + vm_images);
    log.debug("Available: " + vimInstance.getImages());
    if (vm_images != null && !vm_images.isEmpty()) {
      for (String image : vm_images) {
        for (NFVImage nfvImage : vimInstance.getImages()) {
          if (image.equals(nfvImage.getName()) || image.equals(nfvImage.getExtId())) {
            log.info(
                "Image choosed with name: "
                    + nfvImage.getName()
                    + " and ExtId: "
                    + nfvImage.getExtId());
            return nfvImage.getExtId();
          }
        }
      }
      throw new VimException(
          "Not found any Image with name: "
              + vm_images
              + " on VimInstance "
              + vimInstance.getName());
    }
    throw new VimException("No Images are available on VimInstnace " + vimInstance.getName());
  }

  @Override
  public List<Server> queryResources(VimInstance vimInstance) throws VimException {
    log.debug("Listing all VMs of VimInstance " + vimInstance.getName());
    try {
      List<Server> servers = client.listServer(vimInstance);
      log.trace("Listed VMs of VimInstance " + vimInstance.getName() + " -> VMs: " + servers);
      return servers;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not listed VMs successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not listed VMs successfully of VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not listed VMs successfully of VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
  }

  @Override
  public void update(VirtualDeploymentUnit vdu) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void scale(VirtualDeploymentUnit vdu) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void migrate(VirtualDeploymentUnit vdu) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void operate(VirtualDeploymentUnit vdu, String operation) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  @Async
  public Future<Void> release(VNFCInstance vnfcInstance, VimInstance vimInstance)
      throws VimException {
    log.debug(
        "Removing VM with ExtId: "
            + vnfcInstance.getVc_id()
            + " from VimInstance "
            + vimInstance.getName());
    try {
      client.deleteServerByIdAndWait(vimInstance, vnfcInstance.getVc_id());
      log.info(
          "Removed VM with ExtId: "
              + vnfcInstance.getVc_id()
              + " from VimInstance "
              + vimInstance.getName());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(
            "Not removed VM with ExtId "
                + vnfcInstance.getVc_id()
                + " successfully from VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage(),
            e);
      } else {
        log.error(
            "Not removed VM with ExtId "
                + vnfcInstance.getVc_id()
                + " successfully from VimInstance "
                + vimInstance.getName()
                + ". Caused by: "
                + e.getMessage());
      }
      throw new VimException(
          "Not removed VM with ExtId "
              + vnfcInstance.getVc_id()
              + " successfully from VimInstance "
              + vimInstance.getName()
              + ". Caused by: "
              + e.getMessage(),
          e);
    }
    return new AsyncResult<>(null);
  }

  @Override
  public void createReservation(VirtualDeploymentUnit vdu) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void queryReservation() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void updateReservation(VirtualDeploymentUnit vdu) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void releaseReservation(VirtualDeploymentUnit vdu) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public Quota getQuota(VimInstance vimInstance) throws VimException {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  protected VNFCInstance getVnfcInstanceFromServer(
      VimInstance vimInstance,
      VNFComponent vnfComponent,
      String hostname,
      Server server,
      VirtualDeploymentUnit vdu,
      Map<String, String> floatingIps,
      VirtualNetworkFunctionRecord vnfr) {
    VNFCInstance vnfcInstance = new VNFCInstance();
    vnfcInstance.setHostname(hostname);
    vnfcInstance.setVc_id(server.getExtId());
    vnfcInstance.setVim_id(vimInstance.getId());

    vnfcInstance.setConnection_point(new HashSet<VNFDConnectionPoint>());

    for (VNFDConnectionPoint connectionPoint : vnfComponent.getConnection_point()) {
      VNFDConnectionPoint connectionPoint_vnfci = new VNFDConnectionPoint();
      connectionPoint_vnfci.setVirtual_link_reference(connectionPoint.getVirtual_link_reference());
      connectionPoint_vnfci.setType(connectionPoint.getType());
      for (Entry<String, String> entry : server.getFloatingIps().entrySet())
        if (entry.getKey().equals(connectionPoint.getVirtual_link_reference()))
          connectionPoint_vnfci.setFloatingIp(entry.getValue());
      vnfcInstance.getConnection_point().add(connectionPoint_vnfci);
    }

    if (vdu.getVnfc_instance() == null) vdu.setVnfc_instance(new HashSet<VNFCInstance>());

    vnfcInstance.setVnfComponent(vnfComponent);

    vnfcInstance.setIps(new HashSet<Ip>());
    vnfcInstance.setFloatingIps(new HashSet<Ip>());

    if (!floatingIps.isEmpty()) {
      for (Entry<String, String> fip : server.getFloatingIps().entrySet()) {
        Ip ip = new Ip();
        ip.setNetName(fip.getKey());
        ip.setIp(fip.getValue());
        vnfcInstance.getFloatingIps().add(ip);
      }
    }

    for (Entry<String, List<String>> network : server.getIps().entrySet()) {
      Ip ip = new Ip();
      ip.setNetName(network.getKey());
      ip.setIp(network.getValue().iterator().next());
      vnfcInstance.getIps().add(ip);
      for (String ip1 : server.getIps().get(network.getKey())) {
        vnfr.getVnf_address().add(ip1);
      }
    }

    return vnfcInstance;
  }
}
