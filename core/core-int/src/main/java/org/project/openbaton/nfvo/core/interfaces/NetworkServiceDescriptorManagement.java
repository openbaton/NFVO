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

package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.catalogue.mano.common.Security;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.exceptions.BadFormatException;
import org.project.openbaton.exceptions.NetworkServiceIntegrityException;
import org.project.openbaton.exceptions.NotFoundException;

import javax.persistence.NoResultException;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkServiceDescriptorManagement {

    /**
     * This operation allows submitting and
     * validating a Network Service	Descriptor (NSD),
     * including any related VNFFGD and VLD.
     */
    NetworkServiceDescriptor onboard(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, NetworkServiceIntegrityException;

    /**
     * This operation allows disabling a
     * Network Service Descriptor, so that it
     * is not possible to instantiate it any
     * further.
     *
     * @param id
     */
    boolean disable(String id);

    /**
     * This operation allows enabling a
     * Network Service Descriptor.
     *
     * @param id
     */
    boolean enable(String id);

    /**
     * This operation allows updating a Network
     * Service Descriptor (NSD), including any
     * related VNFFGD and VLD.This update might
     * include creating/deleting new VNFFGDs
     * and/or new VLDs.
     *
     * @param new_nsd
     */
    NetworkServiceDescriptor update(NetworkServiceDescriptor new_nsd);

    /**
     * This operation added a new VNFD to the NSD with {@code id}
     *
     * @param vnfd VirtualNetworkFunctionDescriptor to be persisted
     * @param id   of NetworkServiceDescriptor
     * @return the persisted VirtualNetworkFunctionDescriptor
     */
    VirtualNetworkFunctionDescriptor addVnfd(VirtualNetworkFunctionDescriptor vnfd, String id);

    /**
     * This operation is used to query the
     * information of the Network Service
     * Descriptor (NSD), including any
     * related VNFFGD and VLD.
     */
    Iterable<NetworkServiceDescriptor> query();

    NetworkServiceDescriptor query(String id) throws NoResultException;

    /**
     * This operation is used to remove a
     * disabled Network Service Descriptor.
     *
     * @param id
     */
    void delete(String id);

    /**
     * Removes the VNFDescriptor into NSD
     *
     * @param idNsd  of NSD
     * @param idVnfd of VNFD
     */
    void deleteVnfDescriptor(String idNsd, String idVnfd);

    /**
     * Returns the VirtualNetworkFunctionDescriptor selected by idVnfd into NSD with idNsd
     *
     * @param idNsd  of NSD
     * @param idVnfd of VirtualNetworkFunctionDescriptor
     * @return VirtualNetworkFunctionDescriptor
     */
    VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor(String idNsd, String idVnfd) throws NotFoundException;

    /**
     * Updates the VNFDescriptor into NSD with idNsd
     *
     * @param idNsd
     * @param idVfn
     * @param vnfDescriptor
     * @return
     */
    VirtualNetworkFunctionDescriptor updateVNF(String idNsd, String idVfn, VirtualNetworkFunctionDescriptor vnfDescriptor);

    /**
     * Returns the VNFDependency selected by idVnfd into NSD with idNsd
     *
     * @return VNFDependency
     */
    VNFDependency getVnfDependency(String idNsd, String idVnfd);

    /**
     * Removes the VNFDependency into NSD
     *
     * @param idNsd  of NSD
     * @param idVnfd of VNFD
     */
    void deleteVNFDependency(String idNsd, String idVnfd);

    /**
     * Save or Update the VNFDependency into NSD with idNsd
     *
     * @param idNsd
     * @param vnfDependency
     * @return VNFDependency
     */
    VNFDependency saveVNFDependency(String idNsd, VNFDependency vnfDependency);

    /**
     * Deletes the PhysicalNetworkFunctionDescriptor from NSD
     *
     * @param idNsd of NSD
     * @param idPnf of PhysicalNetworkFunctionDescriptor
     */
    void deletePhysicalNetworkFunctionDescriptor(String idNsd, String idPnf);

    /**
     * Returns the PhysicalNetworkFunctionDescriptor with idPnf into NSD with idNsd
     *
     * @param idNsd
     * @param idPnf
     * @return PhysicalNetworkFunctionDescriptor selected
     */
    PhysicalNetworkFunctionDescriptor getPhysicalNetworkFunctionDescriptor(String idNsd, String idPnf) throws NotFoundException;

    /**
     * Adds or Updates the PhysicalNetworkFunctionDescriptor into NSD
     *
     * @param pDescriptor
     * @param id
     * @return PhysicalNetworkFunctionDescriptor
     */
    PhysicalNetworkFunctionDescriptor addPnfDescriptor(PhysicalNetworkFunctionDescriptor pDescriptor, String id);

    /**
     * Adds or Updates the Security into NSD
     *
     * @param id
     * @param security
     * @return
     */
    Security addSecurity(String id, Security security);

    /**
     * Removes the Secuty with idS from NSD with id
     *
     * @param id
     * @param idS
     */
    void deleteSecurty(String id, String idS);
}
