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

package org.openbaton.nfvo.repositories;

import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** Created by mob on 03.09.15. */
@Transactional(readOnly = true)
public class NetworkServiceRecordRepositoryImpl implements NetworkServiceRecordRepositoryCustom {

  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;

  @Autowired private VNFRRepository vnfrRepository;

  @Autowired private VNFRecordDependencyRepository vnfRecordDependencyRepository;

  @Override
  @Transactional
  public VirtualNetworkFunctionRecord addVnfr(VirtualNetworkFunctionRecord vnfr, String id) {
    vnfr = vnfrRepository.save(vnfr);
    networkServiceRecordRepository.findFirstById(id).getVnfr().add(vnfr);
    return vnfr;
  }

  @Override
  @Transactional
  public void deleteVNFRecord(String idNsr, String idVnfd) {
    networkServiceRecordRepository
        .findFirstById(idNsr)
        .getVnfr()
        .remove(vnfrRepository.findOne(idVnfd));
    vnfrRepository.delete(idVnfd);
  }

  @Override
  @Transactional
  public void deleteVNFDependency(String idNsr, String idVnfd) {
    networkServiceRecordRepository
        .findFirstById(idNsr)
        .getVnf_dependency()
        .remove(vnfRecordDependencyRepository.findFirstById(idVnfd));
    vnfRecordDependencyRepository.delete(idVnfd);
  }

  @Override
  @Transactional
  public VNFRecordDependency addVnfRecordDependency(
      VNFRecordDependency vnfRecordDependencyd, String id) {
    vnfRecordDependencyd = vnfRecordDependencyRepository.save(vnfRecordDependencyd);
    networkServiceRecordRepository.findFirstById(id).getVnf_dependency().add(vnfRecordDependencyd);
    return vnfRecordDependencyd;
  }
}
