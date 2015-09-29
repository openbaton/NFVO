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

import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.*;
import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
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
@Scope
public class NetworkServiceDescriptorManagement implements org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NetworkServiceDescriptorRepository nsdRepository;

    @Autowired
    private VNFDRepository vnfdRepository;

    @Autowired
    private VnfmEndpointRepository vnfmManagerEndpointRepository;

    @Autowired
    private VNFDependencyRepository vnfDependencyRepository;

    @Autowired
    private PhysicalNetworkFunctionDescriptorRepository pnfDescriptorRepository;

    @Autowired
    private NSDUtils nsdUtils;

    /**
     * This operation allows submitting and
     * validating a Network Service	Descriptor (NSD),
     * including any related VNFFGD and VLD.
     */
    @Override
    public NetworkServiceDescriptor onboard(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, NetworkServiceIntegrityException {

        nsdUtils.fetchExistingVnfd(networkServiceDescriptor);

        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            if (vnfd.getEndpoint() == null)
                vnfd.setEndpoint(vnfd.getType());
        }

        log.info("Checking if Vnfm is running...");

        Iterable<VnfmManagerEndpoint> endpoints = vnfmManagerEndpointRepository.findAll();

        for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()) {
            boolean found = false;
            for (VnfmManagerEndpoint endpoint : endpoints) {
                if (endpoint.getType().equals(virtualNetworkFunctionDescriptor.getEndpoint())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NotFoundException("VNFManager with endpoint: " + virtualNetworkFunctionDescriptor.getEndpoint() + " is not registered");
            }
        }

        log.trace("Creating " + networkServiceDescriptor);
        log.trace("Fetching Data");
        nsdUtils.fetchVimInstances(networkServiceDescriptor);
        log.trace("Fetched Data");

        log.debug("Checking integrity of NetworkServiceDescriptor");
        nsdUtils.checkIntegrity(networkServiceDescriptor);


        log.trace("Persisting VNFDependencies");
        nsdUtils.fetchDependencies(networkServiceDescriptor);
        log.trace("Persisted VNFDependencies");



        networkServiceDescriptor = nsdRepository.save(networkServiceDescriptor);
        log.debug("Created NetworkServiceDescriptor with id " + networkServiceDescriptor.getId());
        return networkServiceDescriptor;
    }

    /**
     * This operation allows disabling a
     * Network Service Descriptor, so that it
     * is not possible to instantiate it any
     * further.
     *
     * @param id: the id of the {@Link NetworkServiceDescriptor} to disable
     */
    @Override
    public boolean disable(String id) throws NoResultException {
        log.debug("disabling NetworkServiceDescriptor with id " + id);
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(id);
        networkServiceDescriptor.setEnabled(false);
        return networkServiceDescriptor.isEnabled();
    }

    /**
     * This operation allows enabling a
     * Network Service Descriptor.
     *
     * @param id: the id of the {@Link NetworkServiceDescriptor} to enable
     */
    @Override
    public boolean enable(String id) throws NoResultException {
        log.debug("enabling NetworkServiceDescriptor with id " + id);
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(id);
        networkServiceDescriptor.setEnabled(true);
        return networkServiceDescriptor.isEnabled();
    }

    /**
     * This operation allows updating a Network
     * Service Descriptor (NSD), including any
     * related VNFFGD and VLD.This update might
     * include creating/deleting new VNFFGDs
     * and/or new VLDs.
     *
     * @param newNsd: the new values to be updated
     */
    @Override
    public NetworkServiceDescriptor update(NetworkServiceDescriptor newNsd) {
        return nsdRepository.save(newNsd);
    }

    /**
     * This operation added a new VNFD to the NSD with {@code id}
     *
     * @param vnfd VirtualNetworkFunctionDescriptor to be persisted
     * @param id   of NetworkServiceDescriptor
     * @return the persisted VirtualNetworkFunctionDescriptor
     */
    public VirtualNetworkFunctionDescriptor addVnfd(VirtualNetworkFunctionDescriptor vnfd, String id) {
        return nsdRepository.addVnfd(vnfd, id);
    }

    /**
     * Removes the VNFDescriptor with idVnfd from NSD with idNsd
     *
     * @param idNsd  of NSD
     * @param idVnfd of VNFD
     */
    @Override
    public void deleteVnfDescriptor(String idNsd, String idVnfd) {
        log.debug("Removing VnfDescriptor with id: " + idVnfd + " from NSD with id: " + idNsd);
        nsdRepository.deleteVnfd(idNsd, idVnfd);
    }

    /**
     * Returns the VirtualNetworkFunctionDescriptor selected by idVnfd into NSD with idNsd
     *
     * @param idNsd  of NSD
     * @param idVnfd of VirtualNetworkFunctionDescriptor
     * @return VirtualNetworkFunctionDescriptor
     */
    @Override
    public VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor(String idNsd, String idVnfd) throws NotFoundException {
        nsdRepository.exists(idNsd);
        VirtualNetworkFunctionDescriptor firstById = vnfdRepository.findFirstById(idVnfd);
        if (firstById == null)
            throw new NotFoundException("VirtualNetworkFunctionDescriptor with id " + idVnfd + " doesn't exist");
        return firstById;
    }

    /**
     * Updates the VNFDescriptor into NSD with idNsd
     *
     * @param idNsd
     * @param idVfn
     * @param vnfDescriptor
     * @return VirtualNetworkFunctionDescriptor
     */
    @Override
    public VirtualNetworkFunctionDescriptor updateVNF(String idNsd, String idVfn, VirtualNetworkFunctionDescriptor vnfDescriptor) {
        nsdRepository.exists(idNsd);
        nsdRepository.addVnfd(vnfDescriptor, idNsd);
        return vnfDescriptor;
    }

    /**
     * Returns the VNFDependency selected by idVnfd into NSD with idNsd
     *
     * @param idNsd
     * @param idVnfd
     * @return VNFDependency
     */
    @Override
    public VNFDependency getVnfDependency(String idNsd, String idVnfd) {
        nsdRepository.exists(idNsd);
        return vnfDependencyRepository.findOne(idVnfd);
    }

    /**
     * Removes the VNFDependency into NSD
     *
     * @param idNsd  of NSD
     * @param idVnfd of VNFD
     */
    @Override
    public void deleteVNFDependency(String idNsd, String idVnfd) {
        log.debug("Removing VNFDependency with id: " + idVnfd + " from NSD with id: " + idNsd);
        nsdRepository.deleteVNFDependency(idNsd, idVnfd);
        return;
    }

    /**
     * Save or Update the VNFDependency into NSD with idNsd
     *
     * @param idNsd
     * @param vnfDependency
     * @return VNFDependency
     */
    @Override
    public VNFDependency saveVNFDependency(String idNsd, VNFDependency vnfDependency) {
        nsdRepository.addVnfDependency(vnfDependency, idNsd);
        return vnfDependency;
    }

    /**
     * Deletes the PhysicalNetworkFunctionDescriptor from NSD
     *
     * @param idNsd of NSD
     * @param idPnf of PhysicalNetworkFunctionDescriptor
     */
    @Override
    public void deletePhysicalNetworkFunctionDescriptor(String idNsd, String idPnf) {
        nsdRepository.deletePhysicalNetworkFunctionDescriptor(idNsd, idPnf);
    }

    /**
     * Returns the PhysicalNetworkFunctionDescriptor with idPnf into NSD with idNsd
     *
     * @param idNsd
     * @param idPnf
     * @return PhysicalNetworkFunctionDescriptor selected
     */
    @Override
    public PhysicalNetworkFunctionDescriptor getPhysicalNetworkFunctionDescriptor(String idNsd, String idPnf) throws NotFoundException {
        nsdRepository.exists(idNsd);
        PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor = pnfDescriptorRepository.findOne(idPnf);
        if (physicalNetworkFunctionDescriptor == null)
            throw new NotFoundException("PhysicalNetworkFunctionDescriptor with id " + idPnf + " doesn't exist");
        return physicalNetworkFunctionDescriptor;
    }

    /**
     * Add or Update the PhysicalNetworkFunctionDescriptor into NSD
     *
     * @param pDescriptor
     * @param id
     * @return PhysicalNetworkFunctionDescriptor
     */
    @Override
    public PhysicalNetworkFunctionDescriptor addPnfDescriptor(PhysicalNetworkFunctionDescriptor pDescriptor, String id) {
        return nsdRepository.addPnfDescriptor(pDescriptor, id);
    }

    /**
     * Adds or Updates the Security into NSD
     *
     * @param id
     * @param security
     * @return Security
     */
    @Override
    public Security addSecurity(String id, Security security) {
        return nsdRepository.addSecurity(id, security);
    }

    /**
     * Removes the Secuty with idS from NSD with id
     *
     * @param id
     * @param idS
     */
    @Override
    public void deleteSecurty(String id, String idS) {
        nsdRepository.deleteSecurity(id, idS);
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
        return nsdRepository.findFirstById(id);
    }

    /**
     * This operation is used to remove a
     * disabled Network Service Descriptor.
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        log.debug("Removing NetworkServiceDescriptor with id " + id);
        nsdRepository.delete(nsdRepository.findOne(id));
    }
}
