/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.core.api;

import static org.openbaton.nfvo.common.utils.viminstance.VimInstanceUtils.updatePrivateInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.repositories.ImageRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Scope
@ConfigurationProperties
@EnableAsync
public class VimManagement implements org.openbaton.nfvo.core.interfaces.VimManagement {

  @Autowired private VimRepository vimRepository;
  @Autowired private VimBroker vimBroker;
  @Autowired private ImageRepository imageRepository;
  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VNFRRepository vnfrRepository;

  private static Map<String, Long> lastUpdateVim = new ConcurrentHashMap<>();
  private static final Map<String, Object> lockMap = new HashMap<>();

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  // if set to true there will be a check if the stored Vims are still reachable every minute
  @Value("${nfvo.vim.active.check:false}")
  private boolean vimCheck;

  @Value("${nfvo.vim.delete.check.vnfr:true}")
  private boolean checkForVimInVnfr;

  // TODO change Scope to prototype
  @Value("${nfvo.vim.refresh.timout:120}")
  private int refreshTimeout;

  @Value("${nfvo.vim.cache.timout:10000}")
  private long refreshCacheTimeout;

  @Autowired private NetworkServiceRecordRepository nsrRepository;

  @Override
  @Async
  public Future<BaseVimInstance> add(BaseVimInstance vimInstance, String projectId)
      throws VimException, PluginException, IOException, BadRequestException {
    validateVimInstance(vimInstance, projectId);
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

      for (NetworkServiceRecord nsr : nsrRepository.findByProjectId(projectId)) {
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
          for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
            if (vdu.getVimInstanceName().contains(vimInstance.getName())) {
              throw new BadRequestException(
                  "Cannot delete VIM Instance " + vimInstance.getName() + " while it is in use.");
            }
          }
        }
      }
    }
    vimRepository.delete(vimInstance);
  }

  @Override
  @Async
  public Future<BaseVimInstance> update(BaseVimInstance vimInstance, String id, String projectId)
      throws VimException, PluginException, IOException, BadRequestException, NotFoundException {
    validateVimInstance(vimInstance, "");
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
  }

  @Override
  public BaseVimInstance query(String id, String projectId) {
    return vimRepository.findFirstByIdAndProjectId(id, projectId);
  }

  @Override
  @Async
  public Future<BaseVimInstance> refresh(BaseVimInstance vimInstance, boolean force)
      throws VimException, PluginException, IOException {

    if (!force) {
      long lastUpdated = 0;
      try {
        lastUpdated = lastUpdateVim.get(vimInstance.getId());
      } catch (NullPointerException ignored) {

      }
      if (lastUpdated != 0 && (lastUpdated + refreshCacheTimeout) >= new Date().getTime()) {
        return new AsyncResult<>(vimInstance);
      }
    }

    if (vimCheck) {
      this.checkVimInstances();
    } else {
      vimInstance.setActive(true);
    }

    if (!vimInstance.isActive() && vimInstance.getId() != null) {
      return new AsyncResult<>(vimInstance);
    }

    log.info(String.format("Refreshing vim %s", vimInstance.getName()));
    String key = String.format("%s%s", vimInstance.getName(), vimInstance.getProjectId());
    Object lock;
    synchronized (lockMap) {
      lock = lockMap.computeIfAbsent(key, k -> new Object());
    }
    synchronized (lock) {
      int attempt = 0;
      while (true) {
        attempt++;
        vimInstance = vimBroker.getVim(vimInstance.getType()).refresh(vimInstance);
        try {
          vimInstance = vimRepository.save(vimInstance);
        } catch (OptimisticLockingFailureException e) {
          vimInstance = vimRepository.findFirstById(vimInstance.getId());
          if (attempt >= 10)
            throw new VimException(
                "After "
                    + attempt
                    + " attempts it is still not possible to store the VIM instance "
                    + vimInstance.getName(),
                e);
          continue;
        }
        break;
      }
    }

    lastUpdateVim.put(vimInstance.getId(), (new Date()).getTime());
    return new AsyncResult<>(vimInstance);
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
          NotFoundException {
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
          NotFoundException {
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
    return new HashSet<>(vimBroker.getVim(vimInstance.getType()).queryImages(vimInstance));
  }

  @Async
  @Override
  public Future<Void> deleteNetwork(VirtualLinkRecord vlr)
      throws PluginException, NotFoundException, VimException {
    BaseVimInstance vimInstance = this.query(vlr.getVim_id());
    if (vimInstance == null)
      throw new NotFoundException(
          String.format("VimInstance with it %s not found", vlr.getVim_id()));
    vimBroker
        .getVim(vimInstance.getType())
        .delete(
            vimInstance,
            vimInstance
                .getNetworks()
                .parallelStream()
                .filter(n -> n.getExtId().equals(vlr.getExtId()))
                .findFirst()
                .orElseThrow(
                    () ->
                        new NotFoundException(
                            String.format("Network with it %s not found", vlr.getExtId()))));
    return new AsyncResult<>(null);
  }

  @Override
  public BaseVimInstance query(String vimId) {
    return vimRepository.findFirstById(vimId);
  }

  @Override
  public BaseVimInstance queryByProjectIdAndName(String projectId, String name)
      throws NotFoundException {
    BaseVimInstance vim = vimRepository.findByProjectIdAndName(projectId, name);
    if (vim == null) throw new NotFoundException("Not found vim Instance with name " + name);
    return vim;
  }

  /**
   * Validate if the Vim instance has all the required fields filled with values.
   *
   * @param vimInstance the Vim Instance to check
   * @param projectId the project were to save the vim
   * @throws BadRequestException if there is a mistake
   */
  private void validateVimInstance(BaseVimInstance vimInstance, String projectId)
      throws BadRequestException {
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
    if (projectId != null
        && !projectId.equals("")
        && vimRepository.findByProjectIdAndName(projectId, vimInstance.getName()) != null) {
      throw new BadRequestException(
          String.format(
              "Vim Instance with name %s exists already in project %s",
              vimInstance.getName(), projectId));
    }
  }
}
