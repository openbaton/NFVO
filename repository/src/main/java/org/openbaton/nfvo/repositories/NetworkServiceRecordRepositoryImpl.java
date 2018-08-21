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

package org.openbaton.nfvo.repositories;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.NsrNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NetworkServiceRecordRepositoryImpl implements NetworkServiceRecordRepositoryCustom {

  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;

  @Autowired private VNFRRepository vnfrRepository;

  @Autowired private VNFRecordDependencyRepository vnfRecordDependencyRepository;

  @Override
  @Transactional
  public VirtualNetworkFunctionRecord addVnfr(VirtualNetworkFunctionRecord vnfr, String nsrId)
      throws NsrNotFoundException {
    if (networkServiceRecordRepository.exists(nsrId)) {
      vnfr = vnfrRepository.save(vnfr);
      SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
      NetworkServiceRecord nsr = networkServiceRecordRepository.findFirstById(nsrId);
      nsr.setUpdatedAt(format.format(new Date()));
      nsr.getVnfr().add(vnfr);
      return vnfr;
    }
    vnfrRepository.delete(vnfr.getId());
    throw new NsrNotFoundException(
        String.format(
            "NSR with id [%s] does not exist, may be already deleted, ignoring save", nsrId));
  }

  @Override
  @Transactional
  public NetworkServiceRecord saveCascade(NetworkServiceRecord networkServiceRecord) {
    vnfrRepository.save(networkServiceRecord.getVnfr());
    return networkServiceRecordRepository.save(networkServiceRecord);
  }

  @Override
  @Transactional
  public void deleteVNFRecord(String idNsr, String idVnfr) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    NetworkServiceRecord nsr = networkServiceRecordRepository.findFirstById(idNsr);
    if (nsr != null) {
      nsr.setUpdatedAt(format.format(new Date()));
      nsr.getVnfr().remove(vnfrRepository.findFirstById(idVnfr));
    }
    vnfrRepository.delete(idVnfr);
  }

  @Override
  @Transactional
  public void deleteVNFDependency(String idNsr, String idVnfd) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    NetworkServiceRecord nsr = networkServiceRecordRepository.findFirstById(idNsr);
    nsr.setUpdatedAt(format.format(new Date()));
    nsr.getVnf_dependency().remove(vnfRecordDependencyRepository.findFirstById(idVnfd));
    vnfRecordDependencyRepository.delete(idVnfd);
  }

  @Override
  @Transactional
  public VNFRecordDependency addVnfRecordDependency(
      VNFRecordDependency vnfRecordDependencyd, String id) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    NetworkServiceRecord nsr = networkServiceRecordRepository.findFirstById(id);
    nsr.setUpdatedAt(format.format(new Date()));
    vnfRecordDependencyd = vnfRecordDependencyRepository.save(vnfRecordDependencyd);
    nsr.getVnf_dependency().add(vnfRecordDependencyd);
    return vnfRecordDependencyd;
  }
}
