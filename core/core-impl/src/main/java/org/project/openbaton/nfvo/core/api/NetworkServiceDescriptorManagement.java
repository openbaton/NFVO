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

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.project.openbaton.nfvo.repositories.VNFDRepository;
import org.project.openbaton.nfvo.repositories.VNFDependencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope // TODO think if singleton or prototype
public class NetworkServiceDescriptorManagement implements org.project.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NetworkServiceDescriptorRepository nsdRepository;

    @Autowired
    private VNFDRepository vnfdRepository;

    @Autowired
    private NSDUtils nsdUtils;
    @Autowired
    private VNFDependencyRepository vnfDependencyRepository;

    /**
     * This operation allows submitting and
     * validating a Network Service	Descriptor (NSD),
     * including any related VNFFGD and VLD.
     */
    @Override
    public NetworkServiceDescriptor onboard(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        log.trace("Creating " + networkServiceDescriptor);
        log.trace("Fetching Data");
        nsdUtils.fetchVimInstances(networkServiceDescriptor);
        log.trace("Fetched Data");

//        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd())
//            vnfdRepository.create(vnfd);

        log.trace("Persisting VNFDependencies");
        nsdUtils.fetchDependencies(networkServiceDescriptor);
//        for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()){
//            log.trace(""+ vnfDependency.getSource());
//            vnfDependencyRepository.create(vnfDependency);
//        }
        log.trace("Persisted VNFDependencies");

        networkServiceDescriptor=nsdRepository.save(networkServiceDescriptor);
        log.debug("Created NetworkServiceDescriptor with id " + networkServiceDescriptor.getId() );
        return networkServiceDescriptor;
    }

    /**
     * This operation allows disabling a
     * Network Service Descriptor, so that it
     * is not possible to instantiate it any
     * further.
     * @param id: the id of the {@Link NetworkServiceDescriptor} to disable
     */
    @Override
    public boolean disable(String id) throws NoResultException {
        log.debug("disabling NetworkServiceDescriptor with id " + id);
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findOne(id);
        networkServiceDescriptor.setEnabled(false);
        return networkServiceDescriptor.isEnabled();
    }

    /**
     * This operation allows enabling a
     * Network Service Descriptor.
     * @param id: the id of the {@Link NetworkServiceDescriptor} to enable
     */
    @Override
    public boolean enable(String id) throws NoResultException {
        log.debug("enabling NetworkServiceDescriptor with id " + id);
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findOne(id);
        networkServiceDescriptor.setEnabled(true);
        return networkServiceDescriptor.isEnabled();
    }

    /**
     * This operation allows updating a Network
     * Service Descriptor (NSD), including any
     * related VNFFGD and VLD.This update might
     * include creating/deleting new VNFFGDs
     * and/or new VLDs.
     * @param new_nsd: the new values to be updated
     * @param old_id: the id of the old NetworkServiceDescriptor
     */
    @Override
    public NetworkServiceDescriptor update(NetworkServiceDescriptor new_nsd, String old_id) {
        return nsdRepository.save(new_nsd);
    }

    public void removeVNFDescriptor(String id_vnfd){
        vnfdRepository.remove(vnfdRepository.find(id_vnfd));
    }
    /**
     * This operation is used to query the
     * information of the Network Service
     * Descriptor (NSD), including any
     * related VNFFGD and VLD.
     */
    @Override
    public Iterable<NetworkServiceDescriptor> query() {
        return nsdRepository.findAll();
    }

    /**
     * This operation is used to query the
     * information of the Network Service
     * Descriptor (NSD), including any
     * related VNFFGD and VLD.
     */
    @Override
    public NetworkServiceDescriptor query(String id) throws NoResultException {
        return nsdRepository.findOne(id);
    }

    /**
     * This operation is used to remove a
     * disabled Network Service Descriptor.
     * @param id
     */
    @Override
    public void delete(String id) {
        log.debug("Removing NetworkServiceDescriptor with id " + id);
        nsdRepository.delete(nsdRepository.findOne(id));
    }
}
