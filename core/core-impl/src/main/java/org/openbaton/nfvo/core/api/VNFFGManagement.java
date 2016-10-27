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

import org.openbaton.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
import org.openbaton.nfvo.repositories.VNFFGDescriptorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 16/06/15.
 */
@Service
@Scope
public class VNFFGManagement implements org.openbaton.nfvo.core.interfaces.VNFFGManagement {

  @Autowired private VNFFGDescriptorRepository vnffgDescriptorRepository;

  @Override
  public VNFForwardingGraphDescriptor add(
      VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor) {
    return vnffgDescriptorRepository.save(vnfForwardingGraphDescriptor);
  }

  @Override
  public void delete(String id) {
    vnffgDescriptorRepository.delete(vnffgDescriptorRepository.findOne(id));
  }

  @Override
  public Iterable<VNFForwardingGraphDescriptor> query() {
    return vnffgDescriptorRepository.findAll();
  }

  @Override
  public VNFForwardingGraphDescriptor query(String id) {
    return vnffgDescriptorRepository.findOne(id);
  }

  @Override
  public VNFForwardingGraphDescriptor update(
      VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor, String id) {
    throw new UnsupportedOperationException();
  }
}
