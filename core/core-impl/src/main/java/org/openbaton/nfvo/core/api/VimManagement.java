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

import static org.openbaton.nfvo.common.utils.viminstance.VimInstanceUtils.updateBaseNetworks;
import static org.openbaton.nfvo.common.utils.viminstance.VimInstanceUtils.updateNfvImage;
import static org.openbaton.nfvo.common.utils.viminstance.VimInstanceUtils.updatePrivateInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.DockerVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.repositories.ImageRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/** Created by lto on 13/05/15. */
@Service
@Scope
@ConfigurationProperties
public class VimManagement implements org.openbaton.nfvo.core.interfaces.VimManagement {

  @Autowired private VimRepository vimRepository;

  @Autowired private VimBroker vimBroker;

  @Autowired private ImageRepository imageRepository;

  @Autowired private VNFDRepository vnfdRepository;

  @Autowired private VNFRRepository vnfrRepository;

  @Autowired private AsyncVimManagement asyncVimManagement;

  private static Map<String, Long> lastUpdateVim = new ConcurrentHashMap<>();

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  // if set to true there will be a check if the stored Vims are still reachable every minute

  @Value("${nfvo.vim.active.check:false}")
  private boolean vimCheck;

  @Value("${nfvo.vim.delete.check.vnfr:true}")
  private boolean checkForVimInVnfr;

  //TODO change Scope to prototype
  @Value("${nfvo.vim.refresh.timout:120}")
  private int refreshTimeout;

  @Value("${nfvo.vim.cache.timout:10000}")
  private long refreshCacheTimeout;

  public boolean isCheckForVimInVnfr() {
    return checkForVimInVnfr;
  }

  public int getRefreshTimeout() {
    return refreshTimeout;
  }

  public void setRefreshTimeout(int refreshTimeout) {
    this.refreshTimeout = refreshTimeout;
  }

  @Override
  public BaseVimInstance add(BaseVimInstance vimInstance, String projectId)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException {
    validateVimInstance(vimInstance);
    vimInstance.setProjectId(projectId);
    log.trace("Persisting VimInstance: " + vimInstance);
    return this.refresh(vimInstance, true);
  }

  @Override
  public void delete(String id, String projectId) throws NotFoundException, BadRequestException {

    BaseVimInstance vimInstance = vimRepository.findFirstByIdAndProjectId(id, projectId);
    if (vimInstance == null) {
      throw new NotFoundException("Vim Instance with id " + id + " was not found");
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
    vimRepository.delete(vimInstance);
  }

  @Override
  public BaseVimInstance update(BaseVimInstance vimInstance, String id, String projectId)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException, NotFoundException {
    validateVimInstance(vimInstance);
    //    vimInstance = vimRepository.save(vimInstance);
    BaseVimInstance vimInstanceOld = vimRepository.findFirstByIdAndProjectId(id, projectId);
    if (vimInstanceOld == null)
      throw new NotFoundException("VIM Instance with ID " + id + " not found.");
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
    vimInstance.setProjectId(vimInstanceOld.getProjectId());
    vimInstance.setId(vimInstanceOld.getId());
    updatePrivateInfo(vimInstance, vimInstanceOld);
    return refresh(vimInstance, true);
    //    return vimInstance;
  }

  @Override
  public BaseVimInstance query(String id, String projectId) {
    return vimRepository.findFirstByIdAndProjectId(id, projectId);
  }

  @Override
  public synchronized BaseVimInstance refresh(BaseVimInstance vimInstance, boolean force)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException {

    if (!force) {
      long lastUpdated = 0;
      try {
        lastUpdated = lastUpdateVim.get(vimInstance.getId());
      } catch (NullPointerException ignored) {

      }
      if (lastUpdated != 0 && (lastUpdated + refreshCacheTimeout) >= new Date().getTime()) {
        return vimInstance;
      }
    }

    if (vimCheck) {
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

    log.info("Refreshing vim");

    vimInstance = vimBroker.getVim(vimInstance.getType()).refresh(vimInstance);

    vimInstance = vimRepository.save(vimInstance);
    lastUpdateVim.put(vimInstance.getId(), (new Date()).getTime());
    return vimInstance;
  }

  private boolean vimInstanceWithSameNameExists(BaseVimInstance vimInstance) {
    return vimInstance.getId() == null
        && vimRepository.findByProjectIdAndName(vimInstance.getProjectId(), vimInstance.getName())
            == null;
  }

  /**
   * Adds a new NFVImage to the VimInstance with id
   *
   * @param id of VimInstance
   * @param image the new NFVImage
   * @return NFVImage
   */
  @Override
  public BaseNfvImage addImage(String id, BaseNfvImage image, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException, NotFoundException {
    BaseVimInstance vimInstance = vimRepository.findFirstByIdAndProjectId(id, projectId);
    if (vimInstance == null) throw new NotFoundException("No VIMInstance found with ID " + id);
    if (!vimInstance.isActive()) {
      throw new EntityUnreachableException(
          "VimInstance " + vimInstance.getName() + " is not reachable");
    }
    image = vimRepository.addImage(id, image);
    refresh(vimInstance, true);
    return image;
  }

  public BaseNfvImage queryImage(String idVim, String idImage, String projectId)
      throws EntityUnreachableException, NotFoundException {
    BaseVimInstance vimInstance = vimRepository.findFirstByIdAndProjectId(idVim, projectId);
    if (vimInstance == null)
      throw new NotFoundException("VIM Instance with ID " + idVim + " not found.");
    if (!vimInstance.isActive()) {
      throw new EntityUnreachableException(
          "VimInstance " + vimInstance.getName() + " is not reachable");
    }
    try {
      refresh(vimInstance, true);
    } catch (Exception e) {
      log.error(
          "Unable to refresh the VIM instance with ID "
              + idVim
              + " before querying the image with ID "
              + idImage);
      e.printStackTrace();
    }
    for (BaseNfvImage image : vimInstance.getImages()) {
      if (image.getId().equals(idImage)) return imageRepository.findOne(idImage);
    }
    throw new NotFoundException(
        "Did not find image with ID "
            + idImage
            + " for the VIM instance "
            + vimInstance.getName()
            + " with ID "
            + idVim);
  }

  /** Removes the NFVImage with idImage from VimInstance with idVim */
  @Override
  public void deleteImage(String idVim, String idImage, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException, NotFoundException {
    BaseVimInstance vimInstance = vimRepository.findFirstByIdAndProjectId(idVim, projectId);
    if (vimInstance == null)
      throw new NotFoundException("VIM Instance with ID " + idVim + " not found.");
    if (!vimInstance.isActive()) {
      throw new EntityUnreachableException(
          "VimInstance " + vimInstance.getName() + " is not reachable");
    }
    vimRepository.deleteImage(idVim, idImage);
    refresh(vimInstance, false);
  }

  @Override
  public Iterable<BaseVimInstance> queryByProjectId(String projectId) {
    return vimRepository.findByProjectId(projectId);
  }

  /**
   * Checks periodically every minute for the reachability of the VimInstances stored. If a
   * VimInstance is not reachabe it will be set to inactive, otherwise to active.
   */
  @Scheduled(fixedRate = 60000, initialDelay = 10000)
  public synchronized void checkVimInstances() throws IOException {

    if (vimCheck) {
      for (BaseVimInstance vimInstance : vimRepository.findAll()) {
        if (vimInstance.getType().equals("test")) {
          continue;
        }
        URL authUrl;
        try {
          authUrl = new URL(vimInstance.getAuthUrl());
        } catch (MalformedURLException ignored) {
          return;
        }
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

  @Override
  public Set<BaseNfvImage> queryImagesDirectly(BaseVimInstance vimInstance)
      throws PluginException, VimException {

    Set<BaseNfvImage> images = new HashSet<>();
    images.addAll(vimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));

    return images;
  }

  public void setCheckForVimInVnfr(boolean checkForVimInVnfr) {
    this.checkForVimInVnfr = checkForVimInVnfr;
  }

  @Component
  public class AsyncVimManagement {

    @Async
    public Future<Set<BaseNfvImage>> updateImages(BaseVimInstance vimInstance)
        throws PluginException, VimException {
      //Refreshing Images
      Set<BaseNfvImage> baseNfvImagesRefreshed = new HashSet<>();
      Set<BaseNfvImage> imagesNew = new HashSet<>();
      Set<BaseNfvImage> imagesOld = new HashSet<>();
      baseNfvImagesRefreshed.addAll(
          vimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));
      if (vimInstance.getImages() == null) {
        vimInstance.addAllImages(new HashSet<>());
      }
      for (BaseNfvImage nfvImageNew : baseNfvImagesRefreshed) {
        boolean found = false;
        for (BaseNfvImage nfvImageOld : vimInstance.getImages()) {
          if (nfvImageOld.getExtId().equals(nfvImageNew.getExtId())) {
            updateNfvImage(nfvImageNew, nfvImageOld);
            found = true;
            break;
          }
        }
        if (!found) {
          imagesNew.add(nfvImageNew);
        }
      }
      for (BaseNfvImage imageNfvo : vimInstance.getImages()) {
        boolean found = false;
        for (BaseNfvImage imageNew : baseNfvImagesRefreshed) {
          if (imageNfvo.getExtId().equals(imageNew.getExtId())) {
            found = true;
            break;
          }
        }
        if (!found) {
          imagesOld.add(imageNfvo);
        }
      }
      vimInstance.addAllImages(imagesNew);
      vimInstance.getImages().removeAll(imagesOld);
      //      imageRepository.delete(imagesOld);

      return new AsyncResult<>(imagesNew);
    }

    @Async
    public Future<Set<BaseNetwork>> updateNetworks(BaseVimInstance vimInstance)
        throws PluginException, VimException, BadRequestException {
      //Refreshing Networks
      Set<BaseNetwork> networksRefreshed = new HashSet<>();
      Set<BaseNetwork> networksNew = new HashSet<>();
      Set<BaseNetwork> networksOld = new HashSet<>();
      networksRefreshed.addAll(vimBroker.getVim(vimInstance.getType()).queryNetwork(vimInstance));
      if (vimInstance.getNetworks() == null) {
        if (OpenstackVimInstance.class.isInstance(vimInstance))
          ((OpenstackVimInstance) vimInstance).setNetworks(new HashSet<>());
        else if (DockerVimInstance.class.isInstance(vimInstance))
          ((DockerVimInstance) vimInstance).setNetworks(new HashSet<>());
      }
      for (BaseNetwork networkNew : networksRefreshed) {
        boolean found = false;
        for (BaseNetwork networkOld : vimInstance.getNetworks()) {
          log.trace("" + networkOld.getExtId() + " == " + networkNew.getExtId());
          if (networkOld.getExtId() != null && networkNew.getExtId() != null) {
            if (networkOld.getExtId().equals(networkNew.getExtId())) {
              updateBaseNetworks(networkOld, networkNew);
              found = true;
              break;
            }
          }
        }
        if (!found) {
          networksNew.add(networkNew);
        }
      }
      for (BaseNetwork networkNfvo : vimInstance.getNetworks()) {
        boolean found = false;
        for (BaseNetwork network_new : networksRefreshed) {
          if ((networkNfvo.getExtId() == null || network_new.getExtId() == null)
              || networkNfvo.getExtId().equals(network_new.getExtId())) {
            found = true;
            break;
          }
        }
        if (!found) {
          networksOld.add(networkNfvo);
        }
      }
      log.debug("Removing old networks: " + networksOld.size());
      vimInstance.getNetworks().removeAll(networksOld);
      log.debug("Adding new networks: " + networksNew.size());
      vimInstance.addAllNetworks(networksNew);
      //      vimRepository.save(vimInstance);
      //      networkRepository.delete(networksOld);
      return new AsyncResult<>(networksNew);
    }

    @Async
    public Future<Set<DeploymentFlavour>> updateFlavors(BaseVimInstance baseVimInstance)
        throws PluginException, VimException, BadRequestException {
      //Refreshing Flavors
      Set<DeploymentFlavour> flavorsNew = new HashSet<>();
      if (baseVimInstance
          .getClass()
          .getCanonicalName()
          .equals(OpenstackVimInstance.class.getCanonicalName())) {
        OpenstackVimInstance vimInstance = (OpenstackVimInstance) baseVimInstance;
        Set<DeploymentFlavour> flavors_refreshed = new HashSet<>();
        Set<DeploymentFlavour> flavors_old = new HashSet<>();
        flavors_refreshed.addAll(
            vimBroker.getVim(vimInstance.getType()).queryDeploymentFlavors(vimInstance));
        if (vimInstance.getFlavours() == null) {
          vimInstance.setFlavours(new HashSet<>());
        }
        for (DeploymentFlavour flavor_new : flavors_refreshed) {
          boolean found = false;
          for (DeploymentFlavour flavorNfvo : vimInstance.getFlavours()) {
            if (flavorNfvo.getExtId().equals(flavor_new.getExtId())) {
              flavorNfvo.setFlavour_key(flavor_new.getFlavour_key());
              flavorNfvo.setDisk(flavor_new.getDisk());
              flavorNfvo.setRam(flavor_new.getRam());
              flavorNfvo.setVcpus(flavor_new.getVcpus());
              found = true;
              break;
            }
          }
          if (!found) {
            flavorsNew.add(flavor_new);
          }
        }
        for (DeploymentFlavour flavorNfvo : vimInstance.getFlavours()) {
          boolean found = false;
          for (DeploymentFlavour flavor_new : flavors_refreshed) {
            if (flavorNfvo.getExtId().equals(flavor_new.getExtId())) {
              found = true;
              break;
            }
          }
          if (!found) {
            flavors_old.add(flavorNfvo);
          }
        }
        vimInstance.getFlavours().addAll(flavorsNew);
        vimInstance.getFlavours().removeAll(flavors_old);
      }
      return new AsyncResult<>(flavorsNew);
    }
  }

  /**
   * Validate if the Vim instance has all the required fields filled with values.
   *
   * @param vimInstance
   * @throws BadRequestException
   */
  private void validateVimInstance(BaseVimInstance vimInstance) throws BadRequestException {
    if (Objects.equals(vimInstance.getName(), "") || vimInstance.getName() == null)
      throw new BadRequestException("The VIM's name must not be empty or null.");
    if (OpenstackVimInstance.class.isInstance(vimInstance)) {
      if (Objects.equals(((OpenstackVimInstance) vimInstance).getTenant(), "")
          || ((OpenstackVimInstance) vimInstance).getTenant() == null)
        throw new BadRequestException("The VIM's tenant must not be empty or null.");
      if (Objects.equals(((OpenstackVimInstance) vimInstance).getKeyPair(), ""))
        throw new BadRequestException("The VIM's key pair must not be empty or null.");
      if (Objects.equals(((OpenstackVimInstance) vimInstance).getUsername(), "")
          || ((OpenstackVimInstance) vimInstance).getUsername() == null)
        throw new BadRequestException("The VIM's username must not be empty or null.");
      if (((OpenstackVimInstance) vimInstance).getPassword() == null)
        throw new BadRequestException("The VIM's password must not be empty or null.");
    }
    if (Objects.equals(vimInstance.getType(), "") || vimInstance.getType() == null)
      throw new BadRequestException("The VIM's type must not be empty or null.");
  }
}
