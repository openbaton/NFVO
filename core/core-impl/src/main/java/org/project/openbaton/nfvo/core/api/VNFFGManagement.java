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

package org.project.openbaton.nfvo.core.api;

import org.project.openbaton.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 16/06/15.
 */
@Service
@Scope
public class VNFFGManagement implements org.project.openbaton.nfvo.core.interfaces.VNFFGManagement {

    @Autowired
    @Qualifier("VNFFGDescriptorRepository")
    private GenericRepository<VNFForwardingGraphDescriptor> vnffgDescriptorRepository;

    @Override
    public VNFForwardingGraphDescriptor add(VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor) {
        return vnffgDescriptorRepository.create(vnfForwardingGraphDescriptor);
    }

    @Override
    public void delete(String id) {
        vnffgDescriptorRepository.remove(vnffgDescriptorRepository.find(id));
    }

    @Override
    public List<VNFForwardingGraphDescriptor> query() {
        return vnffgDescriptorRepository.findAll();
    }

    @Override
    public VNFForwardingGraphDescriptor query(String id) {
        return vnffgDescriptorRepository.find(id);
    }

    @Override
    public VNFForwardingGraphDescriptor update(VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor, String id) {
        throw new UnsupportedOperationException();
    }
}
