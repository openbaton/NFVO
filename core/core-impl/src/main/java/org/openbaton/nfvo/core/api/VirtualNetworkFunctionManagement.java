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

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

/** Created by mpa on 01.10.15. */
@Service
@Scope
@ConfigurationProperties
public class VirtualNetworkFunctionManagement
    implements org.openbaton.nfvo.core.interfaces.VirtualNetworkFunctionManagement {

  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${vnfd.vnfp.cascade.delete:false}")
  private boolean cascadeDelete;

  @Autowired private NetworkServiceDescriptorRepository nsdRepository;

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public void setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
  }

  @Override
  public VirtualNetworkFunctionDescriptor add(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String projectId)
      throws NotFoundException {
    // TODO check integrity of VNFD
    if (virtualNetworkFunctionDescriptor.getVdu() == null
        || virtualNetworkFunctionDescriptor.getVdu().size() == 0)
      throw new NotFoundException("You should specify at least one VDU in each VNFD!");
    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
      if (vdu.getVnfc() == null || vdu.getVnfc().size() == 0)
        throw new NotFoundException("You should specify at least one VNFC in each VDU!");
    }
    virtualNetworkFunctionDescriptor.setProjectId(projectId);
    return vnfdRepository.save(virtualNetworkFunctionDescriptor);
  }

  @Override
  public void delete(String id, String projectId) throws EntityInUseException {

    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfdRepository.findFirstById(id);
    for (NetworkServiceDescriptor networkServiceDescriptor :
        nsdRepository.findByProjectId(projectId)) {
      for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
        if (vnfd.getId().equals(id)) {
          throw new EntityInUseException(
              "NSD with id: "
                  + networkServiceDescriptor.getId()
                  + " is still onboarded and referencing this VNFD");
        }
      }
    }
    if (!virtualNetworkFunctionDescriptor.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    log.info("Removing VNFD: " + virtualNetworkFunctionDescriptor.getName());
    vnfdRepository.delete(virtualNetworkFunctionDescriptor);
    if (cascadeDelete) {
      log.debug(
          "Removing VNF Package referenced by VNFD " + virtualNetworkFunctionDescriptor.getId());
      if (virtualNetworkFunctionDescriptor.getVnfPackageLocation() != null) {
        log.info(
            "Removing VNF Package with id: "
                + virtualNetworkFunctionDescriptor.getVnfPackageLocation());
        vnfPackageRepository.delete(virtualNetworkFunctionDescriptor.getVnfPackageLocation());
      } else {
        log.debug(
            "No VNFPackage is referenced by VNFD " + virtualNetworkFunctionDescriptor.getId());
      }
    }
  }

  @Override
  public Iterable<VirtualNetworkFunctionDescriptor> query() {
    return vnfdRepository.findAll();
  }

  @Override
  public VirtualNetworkFunctionDescriptor query(String id, String projectId) {
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfdRepository.findFirstById(id);
    if (!virtualNetworkFunctionDescriptor.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    return virtualNetworkFunctionDescriptor;
  }

  @Override
  public VirtualNetworkFunctionDescriptor update(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      String id,
      String projectId) {
    //TODO Update inner fields
    if (!vnfdRepository.findFirstById(id).getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    return vnfdRepository.save(virtualNetworkFunctionDescriptor);
  }

  @Override
  public Iterable<VirtualNetworkFunctionDescriptor> queryByProjectId(String projectId) {
    return vnfdRepository.findByProjectId(projectId);
  }
}
