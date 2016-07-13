/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

package org.openbaton.nfvo.repositories;

import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class NetworkServiceDescriptorRepositoryImpl
    implements NetworkServiceDescriptorRepositoryCustom {

  @Autowired private NetworkServiceDescriptorRepository networkServiceDescriptorRepository;

  @Autowired private VNFDRepository vnfdRepository;

  @Autowired private SecurityRepository securityRepository;

  @Autowired private VNFDependencyRepository vnfDependencyRepository;

  @Autowired private PhysicalNetworkFunctionDescriptorRepository pnfDescriptorRepository;

  @Override
  @Transactional
  public VirtualNetworkFunctionDescriptor addVnfd(
      VirtualNetworkFunctionDescriptor vnfd, String id) {
    vnfd = vnfdRepository.save(vnfd);
    networkServiceDescriptorRepository.findFirstById(id).getVnfd().add(vnfd);
    return vnfd;
  }

  @Override
  @Transactional
  public VNFDependency addVnfDependency(VNFDependency vnfd, String id) {
    vnfd = vnfDependencyRepository.save(vnfd);
    networkServiceDescriptorRepository.findFirstById(id).getVnf_dependency().add(vnfd);
    return vnfd;
  }

  @Override
  @Transactional
  public PhysicalNetworkFunctionDescriptor addPnfDescriptor(
      PhysicalNetworkFunctionDescriptor pnfDescriptor, String id) {
    pnfDescriptor = pnfDescriptorRepository.save(pnfDescriptor);
    networkServiceDescriptorRepository.findFirstById(id).getPnfd().add(pnfDescriptor);
    return pnfDescriptor;
  }

  @Override
  @Transactional
  public Security addSecurity(String id, Security security) {
    security = securityRepository.save(security);
    networkServiceDescriptorRepository.findFirstById(id).setNsd_security(security);
    return security;
  }

  @Override
  @Transactional
  public void deleteSecurity(String id, String idS) {
    Security s = networkServiceDescriptorRepository.findFirstById(id).getNsd_security();
    if (s.getId().equals(securityRepository.findOne(idS).getId())) {
      networkServiceDescriptorRepository.findFirstById(id).setNsd_security(null);
      securityRepository.delete(idS);
    }
  }

  @Override
  @Transactional
  public void deletePhysicalNetworkFunctionDescriptor(String idNsd, String idPnf) {
    networkServiceDescriptorRepository
        .findFirstById(idNsd)
        .getVnfd()
        .remove(pnfDescriptorRepository.findOne(idPnf));
    pnfDescriptorRepository.delete(idPnf);
  }

  @Override
  @Transactional
  public void deleteVnfd(String idNsd, String idVnfd) {
    networkServiceDescriptorRepository
        .findFirstById(idNsd)
        .getVnfd()
        .remove(vnfdRepository.findOne(idVnfd));
    vnfdRepository.delete(idVnfd);
  }

  @Override
  @Transactional
  public void deleteVNFDependency(String idNsd, String idVnfd) {
    networkServiceDescriptorRepository
        .findFirstById(idNsd)
        .getVnf_dependency()
        .remove(vnfDependencyRepository.findOne(idVnfd));
    vnfDependencyRepository.delete(idVnfd);
  }
}
