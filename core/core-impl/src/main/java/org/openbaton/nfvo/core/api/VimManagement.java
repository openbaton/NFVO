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

package org.openbaton.nfvo.core.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.repositories.ImageRepository;
import org.openbaton.nfvo.repositories.NetworkRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

/** Created by lto on 13/05/15. */
@Service
@Scope
public class VimManagement implements org.openbaton.nfvo.core.interfaces.VimManagement {

  @Autowired private VimRepository vimRepository;

  @Autowired private VimBroker vimBroker;

  @Autowired private ImageRepository imageRepository;

  @Autowired private NetworkRepository networkRepository;

  @Autowired private VNFDRepository vnfdRepository;

  @Autowired private VNFRRepository vnfrRepository;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  // if set to true there will be a check if the stored Vims are still reachable every minute
  @Value("${nfvo.vim.active.check:false}")
  private boolean vimCheck;

  @Value("${nfvo.vim.delete.check.vnfr:true}")
  private boolean checkForVimInVnfr;

  public boolean isCheckForVimInVnfr() {
    return checkForVimInVnfr;
  }

  @Override
  public VimInstance add(VimInstance vimInstance, String projectId)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException {
    vimInstance.setProjectId(projectId);
    log.trace("Persisting VimInstance: " + vimInstance);
    return this.refresh(vimInstance);
  }

  @Override
  public void delete(String id, String projectId) throws NotFoundException, BadRequestException {

    VimInstance vimInstance = vimRepository.findFirstById(id);
    if (vimInstance == null) {
      throw new NotFoundException("Vim Instance with id " + id + " was not found");
    }
    if (!vimInstance.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "Vim not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    if (checkForVimInVnfr) {
      for (VirtualNetworkFunctionRecord vnfr : vnfrRepository.findByProjectId(projectId)) {
        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
          if (vdu.getVimInstanceName().contains(vimInstance.getName())) {
            throw new BadRequestException(
                "Cannot delete VIM Instance " + vimInstance.getName() + " while it is in use.");
          }
        }
      }
    }
    //    for (VirtualNetworkFunctionDescriptor vnfd : vnfdRepository.findByProjectId(projectId)) {
    //      for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
    //        if (vdu.getVimInstanceName().contains(vimInstance.getName())) {
    //          vdu.getVimInstanceName().remove(vimInstance.getName());
    //        }
    //      }
    //    }
    vimRepository.delete(vimInstance);
  }

  @Override
  public VimInstance update(VimInstance vimInstance, String id, String projectId)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException {
    if (!vimInstance.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "Vim not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    //    vimInstance = vimRepository.save(vimInstance);
    VimInstance vimInstanceOld = vimRepository.findFirstById(vimInstance.getId());
    if (!vimInstanceOld.getName().equals(vimInstance.getName())) {
      for (VirtualNetworkFunctionDescriptor vnfd : vnfdRepository.findByProjectId(projectId)) {
        for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
          if (vdu.getVimInstanceName().contains(vimInstanceOld.getName())) {
            vdu.getVimInstanceName().remove(vimInstanceOld.getName());
            vdu.getVimInstanceName().add(vimInstance.getName());
          }
        }
      }
      for (VirtualNetworkFunctionRecord vnfr : vnfrRepository.findByProjectId(projectId)) {
        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
          if (vdu.getVimInstanceName().contains(vimInstanceOld.getName())) {
            vdu.getVimInstanceName().remove(vimInstanceOld.getName());
            vdu.getVimInstanceName().add(vimInstance.getName());
          }
        }
      }
    }
    if (vimInstance.getPassword().equals("**********")) {
      vimInstance.setPassword(vimInstanceOld.getPassword());
    }
    return refresh(vimInstance);
    //    return vimInstance;
  }

  @Override
  public VimInstance query(String id, String projectId) {
    VimInstance vimInstance = vimRepository.findFirstById(id);
    if (vimInstance == null) {
      return vimInstance;
    }
    if (!vimInstance.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException("Sorry VimInstance not under the project used");
    }
    return vimInstance;
  }

  @Override
  public VimInstance refresh(VimInstance vimInstance)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException {
    if (vimCheck
        && !vimInstance
            .getType()
            .equals("test")) // just setting it to active without this check may lead to an
    // ObjectOptimisticLockingFailureException
    {
      this.checkVimInstances();
    } else {
      vimInstance.setActive(true);
    }

    if (!vimInstance.isActive() && vimInstance.getId() != null) {
      return vimInstance;
    }

    if (vimInstanceWithSameNameExists(vimInstance)) {
      throw new AlreadyExistingException(
          "VimInstance with name \""
              + vimInstance.getName()
              + "\" already exists in project with id: "
              + vimInstance.getProjectId());
    }

    //Refreshing Images
    Set<NFVImage> images_refreshed = new HashSet<>();
    Set<NFVImage> images_new = new HashSet<>();
    Set<NFVImage> images_old = new HashSet<>();
    images_refreshed.addAll(vimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));
    if (vimInstance.getImages() == null) {
      vimInstance.setImages(new HashSet<NFVImage>());
    }
    for (NFVImage image_new : images_refreshed) {
      boolean found = false;
      for (NFVImage nfvImage_nfvo : vimInstance.getImages()) {
        if (nfvImage_nfvo.getExtId().equals(image_new.getExtId())) {
          nfvImage_nfvo.setName(image_new.getName());
          nfvImage_nfvo.setIsPublic(image_new.isPublic());
          nfvImage_nfvo.setMinRam(image_new.getMinRam());
          nfvImage_nfvo.setMinCPU(image_new.getMinCPU());
          nfvImage_nfvo.setMinDiskSpace(image_new.getMinDiskSpace());
          nfvImage_nfvo.setDiskFormat(image_new.getDiskFormat());
          nfvImage_nfvo.setContainerFormat(image_new.getContainerFormat());
          nfvImage_nfvo.setCreated(image_new.getCreated());
          nfvImage_nfvo.setUpdated(image_new.getUpdated());
          found = true;
          break;
        }
      }
      if (!found) {
        images_new.add(image_new);
      }
    }
    for (NFVImage nfvImage_nfvo : vimInstance.getImages()) {
      boolean found = false;
      for (NFVImage image_new : images_refreshed) {
        if (nfvImage_nfvo.getExtId().equals(image_new.getExtId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        images_old.add(nfvImage_nfvo);
      }
    }
    vimInstance.getImages().addAll(images_new);
    vimInstance.getImages().removeAll(images_old);
    imageRepository.delete(images_old);
    //Refreshing Networks
    Set<Network> networks_refreshed = new HashSet<>();
    Set<Network> networks_new = new HashSet<>();
    Set<Network> networks_old = new HashSet<>();
    networks_refreshed.addAll(vimBroker.getVim(vimInstance.getType()).queryNetwork(vimInstance));
    if (vimInstance.getNetworks() == null) {
      vimInstance.setNetworks(new HashSet<Network>());
    }
    for (Network network_new : networks_refreshed) {
      boolean found = false;
      for (Network network_nfvo : vimInstance.getNetworks()) {
        if (network_nfvo.getExtId().equals(network_new.getExtId())) {
          network_nfvo.setName(network_new.getName());
          network_nfvo.setExternal(network_new.getExternal());
          network_nfvo.setShared(network_new.getExternal());
          Set<Subnet> subnets_refreshed = new HashSet<>();
          Set<Subnet> subnets_new = new HashSet<>();
          Set<Subnet> subnets_old = new HashSet<>();
          if (network_new.getSubnets() == null && !vimInstance.getType().equals("test")) {
            throw new BadRequestException(
                "New network: " + network_new.getName() + " has no subnets");
          } else if (network_new.getSubnets() == null) {
            network_new.setSubnets(new HashSet<Subnet>());
          }
          subnets_refreshed.addAll(network_new.getSubnets());
          if (network_nfvo.getSubnets() == null) {
            network_nfvo.setSubnets(new HashSet<Subnet>());
          }
          for (Subnet subnet_new : subnets_refreshed) {
            boolean found_subnet = false;
            for (Subnet subnet_nfvo : network_nfvo.getSubnets()) {
              if (subnet_nfvo.getExtId().equals(subnet_new.getExtId())) {
                subnet_nfvo.setName(subnet_new.getName());
                subnet_nfvo.setNetworkId(subnet_new.getNetworkId());
                subnet_nfvo.setGatewayIp(subnet_new.getGatewayIp());
                subnet_nfvo.setCidr(subnet_new.getCidr());
                found_subnet = true;
                break;
              }
            }
            if (!found_subnet) {
              subnets_new.add(subnet_new);
            }
          }
          for (Subnet subnet_nfvo : network_nfvo.getSubnets()) {
            boolean found_subnet = false;
            for (Subnet subnet_new : subnets_refreshed) {
              if (subnet_nfvo.getExtId().equals(subnet_new.getExtId())) {
                found_subnet = true;
                break;
              }
            }
            if (!found_subnet) {
              subnets_old.add(subnet_nfvo);
            }
          }
          network_nfvo.getSubnets().addAll(subnets_new);
          network_nfvo.getSubnets().removeAll(subnets_old);
          found = true;
          break;
        }
      }
      if (!found) {
        networks_new.add(network_new);
      }
    }
    for (Network network_nfvo : vimInstance.getNetworks()) {
      boolean found = false;
      for (Network network_new : networks_refreshed) {
        if (network_nfvo.getExtId().equals(network_new.getExtId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        networks_old.add(network_nfvo);
      }
    }
    vimInstance.getNetworks().addAll(networks_new);
    vimInstance.getNetworks().removeAll(networks_old);
    networkRepository.delete(networks_old);
    //Refreshing Flavors
    Set<DeploymentFlavour> flavors_refreshed = new HashSet<>();
    Set<DeploymentFlavour> flavors_new = new HashSet<>();
    Set<DeploymentFlavour> flavors_old = new HashSet<>();
    flavors_refreshed.addAll(
        vimBroker.getVim(vimInstance.getType()).queryDeploymentFlavors(vimInstance));
    if (vimInstance.getFlavours() == null) {
      vimInstance.setFlavours(new HashSet<DeploymentFlavour>());
    }
    for (DeploymentFlavour flavor_new : flavors_refreshed) {
      boolean found = false;
      for (DeploymentFlavour flavor_nfvo : vimInstance.getFlavours()) {
        if (flavor_nfvo.getExtId().equals(flavor_new.getExtId())) {
          flavor_nfvo.setFlavour_key(flavor_new.getFlavour_key());
          flavor_nfvo.setDisk(flavor_new.getDisk());
          flavor_nfvo.setRam(flavor_new.getRam());
          flavor_nfvo.setVcpus(flavor_new.getVcpus());
          found = true;
          break;
        }
      }
      if (!found) {
        flavors_new.add(flavor_new);
      }
    }
    for (DeploymentFlavour flavor_nfvo : vimInstance.getFlavours()) {
      boolean found = false;
      for (DeploymentFlavour flavor_new : flavors_refreshed) {
        if (flavor_nfvo.getExtId().equals(flavor_new.getExtId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        flavors_old.add(flavor_nfvo);
      }
    }
    vimInstance.getFlavours().addAll(flavors_new);
    vimInstance.getFlavours().removeAll(flavors_old);
    return vimRepository.save(vimInstance);
  }

  private boolean vimInstanceWithSameNameExists(VimInstance vimInstance) {
    if (vimInstance.getId() == null) {
      for (VimInstance vimInstance1 : vimRepository.findByProjectId(vimInstance.getProjectId())) {
        if (vimInstance1.getName().equals(vimInstance.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Adds a new NFVImage to the VimInstance with id
   *
   * @param id of VimInstance
   * @param image the new NFVImage
   * @return NFVImage
   */
  @Override
  public NFVImage addImage(String id, NFVImage image, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException {
    VimInstance vimInstance = vimRepository.findFirstById(id);
    if (!vimInstance.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VimInstance not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    if (!vimInstance.isActive()) {
      throw new EntityUnreachableException(
          "VimInstance " + vimInstance.getName() + " is not reachable");
    }
    image = vimRepository.addImage(id, image);
    refresh(vimInstance);
    return image;
  }

  public NFVImage queryImage(String idVim, String idImage, String projectId)
      throws EntityUnreachableException {
    VimInstance vimInstance = vimRepository.findFirstById(idVim);
    if (vimInstance.getProjectId().equals(projectId)) {
      if (!vimInstance.isActive()) {
        throw new EntityUnreachableException(
            "VimInstance " + vimInstance.getName() + " is not reachable");
      }
      return imageRepository.findOne(idImage);
    }
    throw new UnauthorizedUserException(
        "VimInstance not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /** Removes the NFVImage with idImage from VimInstance with idVim */
  @Override
  public void deleteImage(String idVim, String idImage, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException {
    VimInstance vimInstance = vimRepository.findFirstById(idVim);
    if (!vimInstance.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VimInstance not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    if (!vimInstance.isActive()) {
      throw new EntityUnreachableException(
          "VimInstance " + vimInstance.getName() + " is not reachable");
    }
    vimRepository.deleteImage(idVim, idImage);
    refresh(vimInstance);
  }

  @Override
  public Iterable<VimInstance> queryByProjectId(String projectId) {
    return vimRepository.findByProjectId(projectId);
  }

  /**
   * Checks periodically every minute for the reachability of the VimInstances stored. If a
   * VimInstance is not reachabe it will be set to inactive, otherwise to active.
   */
  @Scheduled(fixedRate = 60000, initialDelay = 10000)
  private synchronized void checkVimInstances() throws IOException {

    if (vimCheck) {
      for (VimInstance vimInstance : vimRepository.findAll()) {
        if (vimInstance.getType().equals("test")) {
          continue;
        }

        URL authUrl = new URL(vimInstance.getAuthUrl());
        log.trace(
            "Checking host: " + authUrl.getHost() + " of VimInstance " + vimInstance.getName());
        byte[] bytes = authUrl.getHost().getBytes();

        if (vimInstance.isActive() && !InetAddress.getByName(authUrl.getHost()).isReachable(5000)) {
          log.debug(
              "Authentication url "
                  + vimInstance.getAuthUrl()
                  + " of VimInstance "
                  + vimInstance.getName()
                  + " with id "
                  + vimInstance.getId()
                  + " is not reachable anymore. Set the VimInstance to not active.");
          vimInstance.setActive(false);
          vimRepository.save(vimInstance);
        } else if (!vimInstance.isActive()
            && InetAddress.getByName(authUrl.getHost()).isReachable(5000)) {
          log.debug(
              "Authentication url "
                  + vimInstance.getAuthUrl()
                  + " of non active VimInstance "
                  + vimInstance.getName()
                  + " with id "
                  + vimInstance.getId()
                  + " is reachable. Set the VimInstance to active.");
          vimInstance.setActive(true);
          vimRepository.save(vimInstance);
        }
      }
    }
  }

  public void setCheckForVimInVnfr(boolean checkForVimInVnfr) {
    this.checkForVimInVnfr = checkForVimInVnfr;
  }
}
