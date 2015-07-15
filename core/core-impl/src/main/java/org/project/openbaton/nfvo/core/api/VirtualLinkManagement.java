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

import org.project.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.project.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.List;

/**
 *
 * Created by lto on 11/06/15.
 */
@Service
@Scope
public class VirtualLinkManagement implements org.project.openbaton.nfvo.core.interfaces.VirtualLinkManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("virtualLinkDescriptorRepository")
    private GenericRepository<VirtualLinkDescriptor> virtualLinkDescriptorRepository;

    @Autowired
    @Qualifier("virtualLinkRecordRepository")
    private GenericRepository<VirtualLinkRecord> virtualLinkRecordRepository;

    @Override
    public VirtualLinkDescriptor add(VirtualLinkDescriptor virtualLinkDescriptor) {
        log.trace("Adding VirtualLinkDescriptor " + virtualLinkDescriptor);
        log.debug("Adding VirtualLinkDescriptor with Id " + virtualLinkDescriptor.getId());
        //TODO maybe check whenever the image is available on the VimInstance
        return virtualLinkDescriptorRepository.create(virtualLinkDescriptor);
    }

    @Override
    public VirtualLinkRecord add(VirtualLinkRecord virtualLinkRecord) {
        log.trace("Adding VirtualLinkDescriptor " + virtualLinkRecord);
        log.debug("Adding VirtualLinkDescriptor with Id " + virtualLinkRecord.getId());
        //TODO maybe check whenever the image is available on the VimInstance
        return virtualLinkRecordRepository.create(virtualLinkRecord);
    }

    @Override
    public void delete(String id) {
        log.debug("Removing image with id " + id);
        VirtualLinkDescriptor vld = null;
        VirtualLinkRecord vlr = null;
        try {
            vld = virtualLinkDescriptorRepository.find(id);
        } catch (NoResultException e) {
            vlr = virtualLinkRecordRepository.find(id);
        }

        if (vld == null)
            virtualLinkRecordRepository.remove(vlr);
        else
            virtualLinkDescriptorRepository.remove(vld);
    }

    @Override
    public VirtualLinkDescriptor update(VirtualLinkDescriptor virtualLinkDescriptor_new, String id) {
        VirtualLinkDescriptor old = virtualLinkDescriptorRepository.find(id);
        old.setConnection(virtualLinkDescriptor_new.getConnection());
        old.setQos(virtualLinkDescriptor_new.getQos());
        old.setDescriptor_version(virtualLinkDescriptor_new.getDescriptor_version());
        old.setNumber_of_endpoints(virtualLinkDescriptor_new.getNumber_of_endpoints());
        old.setVendor(virtualLinkDescriptor_new.getVendor());
        old.setVld_security(virtualLinkDescriptor_new.getVld_security());
        old.setConnectivity_type(virtualLinkDescriptor_new.getConnectivity_type());
        old.setLeaf_requirement(virtualLinkDescriptor_new.getLeaf_requirement());
        old.setRoot_requirement(virtualLinkDescriptor_new.getRoot_requirement());
        old.setTest_access(virtualLinkDescriptor_new.getTest_access());
        return old;
    }

    @Override
    public VirtualLinkRecord update(VirtualLinkRecord virtualLinkRecord_new, String id) {
        VirtualLinkRecord old = virtualLinkRecordRepository.find(id);
        old.setConnection(virtualLinkRecord_new.getConnection());
        old.setQos(virtualLinkRecord_new.getQos());
        old.setVendor(virtualLinkRecord_new.getVendor());
        old.setAllocated_capacity(virtualLinkRecord_new.getAllocated_capacity());
        old.setConnectivity_type(virtualLinkRecord_new.getConnectivity_type());
        old.setLeaf_requirement(virtualLinkRecord_new.getLeaf_requirement());
        old.setRoot_requirement(virtualLinkRecord_new.getRoot_requirement());
        old.setTest_access(virtualLinkRecord_new.getTest_access());
        old.setAudit_log(virtualLinkRecord_new.getAudit_log());
        old.setDescriptor_reference(virtualLinkRecord_new.getDescriptor_reference());
        old.setLifecycle_event_history(virtualLinkRecord_new.getLifecycle_event_history());
        old.setNotification(virtualLinkRecord_new.getNotification());
        old.setParent_ns(virtualLinkRecord_new.getParent_ns());
        old.setStatus(virtualLinkRecord_new.getStatus());
        old.setVnffgr_reference(virtualLinkRecord_new.getVnffgr_reference());
        old.setVim_id(virtualLinkRecord_new.getVim_id());
        old.setVersion(virtualLinkRecord_new.getVersion());
        return old;
    }

    @Override
    public List<VirtualLinkDescriptor> queryDescriptors() {
        return virtualLinkDescriptorRepository.findAll();
    }

    @Override
    public List<VirtualLinkRecord> queryRecords() {
        return virtualLinkRecordRepository.findAll();
    }

    @Override
    public VirtualLinkRecord queryRecord(String id) {
        return virtualLinkRecordRepository.find(id);
    }

    @Override
    public VirtualLinkDescriptor queryDescriptor(String id){
        return virtualLinkDescriptorRepository.find(id);
    }
}
