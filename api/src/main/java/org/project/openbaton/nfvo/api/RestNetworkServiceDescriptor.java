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

package org.project.openbaton.nfvo.api;

import org.project.openbaton.catalogue.mano.common.Security;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.exceptions.*;
import org.project.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.project.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.project.openbaton.nfvo.core.interfaces.SecurityManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/ns-descriptors")
public class RestNetworkServiceDescriptor {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SecurityManagement securityManagement;

    @Autowired
    private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;

    @Autowired
    private NetworkServiceRecordManagement networkServiceRecordManagement;

    /**
     * This operation allows submitting and validating a Network Service
     * Descriptor (NSD), including any related VNFFGD and VLD.
     *
     * @param networkServiceDescriptor : the Network Service Descriptor to be created
     * @return networkServiceDescriptor: the Network Service Descriptor filled
     * with id and values from core
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkServiceDescriptor create(@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, NetworkServiceIntegrityException {
        NetworkServiceDescriptor nsd;
        log.trace("Just Received: " + networkServiceDescriptor);
        nsd = networkServiceDescriptorManagement.onboard(networkServiceDescriptor);
        return nsd;
    }

    /**
     * This operation is used to remove a disabled Network Service Descriptor
     *
     * @param id of Network Service Descriptor
     */
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        networkServiceDescriptorManagement.delete(id);

    }

    /**
     * This operation returns the list of Network Service Descriptor (NSD)
     *
     * @return List<NetworkServiceDescriptor>: the list of Network Service
     * Descriptor stored
     */

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<NetworkServiceDescriptor> findAll() {
        return networkServiceDescriptorManagement.query();
    }

    /**
     * This operation returns the Network Service Descriptor (NSD) selected by
     * id
     *
     * @param id of Network Service Descriptor
     * @return NetworkServiceDescriptor: the Network Service Descriptor selected
     * @
     */

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public NetworkServiceDescriptor findById(@PathVariable("id") String id) {
        NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id);
        return nsd;
    }

    /**
     * This operation updates the Network Service Descriptor (NSD)
     *
     * @param networkServiceDescriptor : the Network Service Descriptor to be updated
     * @param id                       : the id of Network Service Descriptor
     * @return networkServiceDescriptor: the Network Service Descriptor updated
     */

    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NetworkServiceDescriptor update(
            @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
            @PathVariable("id") String id) {
        return networkServiceDescriptorManagement.update(networkServiceDescriptor);
    }

    /**
     * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
     *
     * @param id : The id of NSD
     * @return List<VirtualNetworkFunctionDescriptor>: The List of
     * VirtualNetworkFunctionDescriptor into NSD
     * @
     */
    @RequestMapping(value = "{id}/vnfdescriptors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Set<VirtualNetworkFunctionDescriptor> getVirtualNetworkFunctionDescriptors(
            @PathVariable("id") String id) {
        NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id);
        return nsd.getVnfd();
    }

    @RequestMapping(value = "{idNsd}/vnfdescriptors/{idVfnd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor(
            @PathVariable("idNsd") String idNsd, @PathVariable("idVfnd") String idVfnd) throws NotFoundException {
        return networkServiceDescriptorManagement.getVirtualNetworkFunctionDescriptor(idNsd, idVfnd);
    }

    @RequestMapping(value = "{idNsd}/vnfdescriptors/{idVfn}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVirtualNetworkFunctionDescriptor(
            @PathVariable("idNsd") String idNsd, @PathVariable("idVfn") String idVfn) throws NotFoundException {
        networkServiceDescriptorManagement.deleteVnfDescriptor(idNsd, idVfn);
    }

    @RequestMapping(value = "{id}/vnfdescriptors/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VirtualNetworkFunctionDescriptor postVNFD(
            @RequestBody @Valid VirtualNetworkFunctionDescriptor vnfDescriptor,
            @PathVariable("id") String id) {
        return networkServiceDescriptorManagement.addVnfd(vnfDescriptor, id);
    }

    @RequestMapping(value = "{idNsd}/vnfdescriptors/{idVfn}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VirtualNetworkFunctionDescriptor updateVNF(         @RequestBody @Valid VirtualNetworkFunctionDescriptor vnfDescriptor,           @PathVariable("idNsd") String idNsd, @PathVariable("idVfn") String idVfn) {
        return networkServiceDescriptorManagement.updateVNF(idNsd, idVfn, vnfDescriptor);
    }

    /**
     * Returns the list of VNFDependency into a NSD with id
     *
     * @param id : The id of NSD
     * @return List<VNFDependency>: The List of VNFDependency into NSD
     * @
     */

    @RequestMapping(value = "{id}/vnfdependencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Set<VNFDependency> getVNFDependencies(@PathVariable("id") String id) {
        NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id);
        return nsd.getVnf_dependency();
    }

    @RequestMapping(value = "{idNsd}/vnfdependencies/{idVnfd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VNFDependency getVNFDependency(@PathVariable("idNsd") String idNsd,
                                          @PathVariable("idVnfd") String idVnfd) throws NotFoundException {

        return networkServiceDescriptorManagement.getVnfDependency(idNsd, idVnfd);
    }

    @RequestMapping(value = "{idNsd}/vnfdependencies/{idVnfd}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVNFDependency(@PathVariable("idNsd") String idNsd,
                                    @PathVariable("idVnfd") String idVnfd) throws NotFoundException {
        networkServiceDescriptorManagement.deleteVNFDependency(idNsd, idVnfd);
    }

    @RequestMapping(value = "{idNsd}/vnfdependencies/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VNFDependency postVNFDependency(@RequestBody @Valid VNFDependency vnfDependency, @PathVariable("idNsd") String idNsd) {
        networkServiceDescriptorManagement.saveVNFDependency(idNsd, vnfDependency);
        return vnfDependency;
    }

    @RequestMapping(value = "{idNsd}/vnfdependencies/{idVnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VNFDependency updateVNFDependency(
            @RequestBody @Valid VNFDependency vnfDependency,
            @PathVariable("idNsd") String idNsd,
            @PathVariable("idVnf") String idVnf) {
        networkServiceDescriptorManagement.saveVNFDependency(idNsd, vnfDependency);
        return vnfDependency;
    }

    /**
     * Returns the list of PhysicalNetworkFunctionDescriptor into a NSD with id
     *
     * @param id : The id of NSD
     * @return List<PhysicalNetworkFunctionDescriptor>: The List of
     * PhysicalNetworkFunctionDescriptor into NSD
     * @
     */
    @RequestMapping(value = "{id}/pnfdescriptors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Set<PhysicalNetworkFunctionDescriptor> getPhysicalNetworkFunctionDescriptors(
            @PathVariable("id") String id) {
        NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id);
        return nsd.getPnfd();
    }

    /**
     * Returns the PhysicalNetworkFunctionDescriptor
     *
     * @param idNsd : The NSD id
     * @param idPnf : The PhysicalNetworkFunctionDescriptor id
     * @return PhysicalNetworkFunctionDescriptor: The
     * PhysicalNetworkFunctionDescriptor selected
     * @
     */

    @RequestMapping(value = "{idNds}/pnfdescriptors/{idPnf}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PhysicalNetworkFunctionDescriptor getPhysicalNetworkFunctionDescriptor(
            @PathVariable("idNds") String idNsd, @PathVariable("idPnf") String idPnf) throws NotFoundException {
        return networkServiceDescriptorManagement.getPhysicalNetworkFunctionDescriptor(idNsd, idPnf);
    }

    /**
     * Deletes the PhysicalNetworkFunctionDescriptor with the idPnf
     *
     * @param idNsd id of NSD
     * @param idPnf id of PhysicalNetworkFunctionDescriptor
     */
    @RequestMapping(value = "{idNsd}/pnfdescriptors/{idPnf}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhysicalNetworkFunctionDescriptor(
            @PathVariable("idNsd") String idNsd, @PathVariable("idPnf") String idPnf) throws NotFoundException {
        networkServiceDescriptorManagement.deletePhysicalNetworkFunctionDescriptor(idNsd, idPnf);
    }

    /**
     * Stores the PhysicalNetworkFunctionDescriptor
     *
     * @param pDescriptor : The PhysicalNetworkFunctionDescriptor to be stored
     * @param id          : The NSD id
     * @return PhysicalNetworkFunctionDescriptor: The
     * PhysicalNetworkFunctionDescriptor stored
     * @
     */
    @RequestMapping(value = "{id}/pnfdescriptors/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PhysicalNetworkFunctionDescriptor postPhysicalNetworkFunctionDescriptor(@RequestBody @Valid PhysicalNetworkFunctionDescriptor pDescriptor, @PathVariable("id") String id) {
        return networkServiceDescriptorManagement.addPnfDescriptor(pDescriptor, id);
    }

    /**
     * Edits the PhysicalNetworkFunctionDescriptor
     *
     * @param pDescriptor : The PhysicalNetworkFunctionDescriptor to be edited
     * @param id          : The NSD id
     * @return PhysicalNetworkFunctionDescriptor: The
     * PhysicalNetworkFunctionDescriptor edited
     * @
     */
    @RequestMapping(value = "{id}/pnfdescriptors/{idPnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PhysicalNetworkFunctionDescriptor updatePNFD(@RequestBody @Valid PhysicalNetworkFunctionDescriptor pDescriptor, @PathVariable("id") String id, @PathVariable("idPnf") String idPnf) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the Security into a NSD with id
     *
     * @param id : The id of NSD
     * @return Security: The Security of PhysicalNetworkFunctionDescriptor into
     * NSD
     * @
     */

    @RequestMapping(value = "{id}/security", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Security getSecurity(@PathVariable("id") String id) {
        NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id);
        return nsd.getNsd_security();
    }

//	/**
//	 * Returns the Security with the id_s
//	 *
//	 * @param id
//	 *            : The NSD id
//	 * @param id_s
//	 *            : The Security id
//	 * @return Security: The Security selected by id_s
//	 * @
//	 */
//	@RequestMapping(value = "{id}/security/{id_s}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	@ResponseStatus(HttpStatus.ACCEPTED)
//	public Security getSecurity(@PathVariable("id") String id,
//			@PathVariable("id_s") String id_s)  {
//		NetworkServiceDescriptor nsd = null;
//		try {
//			nsd = networkServiceDescriptorManagement.query(id);
//		} catch (NoResultException e) {
//
//			log.error(e.getMessage(), e);
//			throw new NSDNotFoundException(id);
//		}
//		if (!nsd.getNsd_security().getId().equals(id_s)) {
//			log.error("Security with id: " + id_s + " not found.");
//			throw new NSDNotFoundException(id_s);
//		}
//		return nsd.getNsd_security();
//	}

    /**
     * Deletes the Security with the id_s
     *
     * @param id  : The NSD id
     * @param idS : The Security id
     * @
     */
    @RequestMapping(value = "{id}/security/{idS}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecurity(@PathVariable("id") String id,
                               @PathVariable("idS") String idS) {

        networkServiceDescriptorManagement.deleteSecurty(id, idS);

    }

    /**
     * Stores the Security into NSD
     *
     * @param security : The Security to be stored
     * @param id       : The id of NSD
     * @return Security: The Security stored
     * @
     */
    @RequestMapping(value = "{id}/security/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Security postSecurity(@RequestBody @Valid Security security, @PathVariable("id") String id) {
        return networkServiceDescriptorManagement.addSecurity(id, security);
    }

    @RequestMapping(value = "{id}/security/{id_s}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Security updateSecurity(@RequestBody @Valid Security security,
                                   @PathVariable("id") String id, @PathVariable("id_s") String id_s) {
//        NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id);
//        nsd.setNsd_security(security);
//        networkServiceDescriptorManagement.update(nsd);

        return security;
    }

    @RequestMapping(value = "/records", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkServiceRecord createRecord(
            @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) throws BadFormatException, InterruptedException, ExecutionException, VimException, NotFoundException, VimDriverException, QuotaExceededException {
        return networkServiceRecordManagement.onboard(networkServiceDescriptor);
    }


}