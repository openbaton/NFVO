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

package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.nfvo.repositories.VirtualLinkDescriptorRepository;
import org.openbaton.nfvo.repositories.VirtualLinkRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;

/**
 * Created by lto on 11/06/15.
 */
@Service
@Scope
public class VirtualLinkManagement implements org.openbaton.nfvo.core.interfaces.VirtualLinkManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VirtualLinkDescriptorRepository virtualLinkDescriptorRepository;

    @Autowired
    private VirtualLinkRecordRepository virtualLinkRecordRepository;

    @Override
    public VirtualLinkDescriptor add(VirtualLinkDescriptor virtualLinkDescriptor) {
        log.trace("Adding VirtualLinkDescriptor " + virtualLinkDescriptor);
        log.debug("Adding VirtualLinkDescriptor with Id " + virtualLinkDescriptor.getId());
        return virtualLinkDescriptorRepository.save(virtualLinkDescriptor);
    }

    @Override
    public VirtualLinkRecord add(VirtualLinkRecord virtualLinkRecord) {
        log.trace("Adding VirtualLinkDescriptor " + virtualLinkRecord);
        log.debug("Adding VirtualLinkDescriptor with Id " + virtualLinkRecord.getId());
        return virtualLinkRecordRepository.save(virtualLinkRecord);
    }

    @Override
    public void delete(String id) {
        log.debug("Removing image with id " + id);
        VirtualLinkDescriptor vld = null;
        VirtualLinkRecord vlr = null;
        try {
            vld = virtualLinkDescriptorRepository.findOne(id);
        } catch (NoResultException e) {
            vlr = virtualLinkRecordRepository.findOne(id);
        }

        if (vld == null)
            virtualLinkRecordRepository.delete(vlr);
        else
            virtualLinkDescriptorRepository.delete(vld);
    }

    @Override
    public VirtualLinkDescriptor update(VirtualLinkDescriptor virtualLinkDescriptor, String id) {
        virtualLinkDescriptor = virtualLinkDescriptorRepository.save(virtualLinkDescriptor);
        return virtualLinkDescriptor;
    }

    @Override
    public VirtualLinkRecord update(VirtualLinkRecord virtualLinkRecord, String id) {
        virtualLinkRecord = virtualLinkRecordRepository.save(virtualLinkRecord);
        return virtualLinkRecord;
    }

    @Override
    public Iterable<VirtualLinkDescriptor> queryDescriptors() {
        return virtualLinkDescriptorRepository.findAll();
    }

    @Override
    public Iterable<VirtualLinkRecord> queryRecords() {
        return virtualLinkRecordRepository.findAll();
    }

    @Override
    public VirtualLinkRecord queryRecord(String id) {
        return virtualLinkRecordRepository.findOne(id);
    }

    @Override
    public VirtualLinkDescriptor queryDescriptor(String id) {
        return virtualLinkDescriptorRepository.findOne(id);
    }
}
