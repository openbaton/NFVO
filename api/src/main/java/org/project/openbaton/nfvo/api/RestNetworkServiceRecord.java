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

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.PhysicalNetworkFunctionRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.nfvo.api.exceptions.*;
import org.project.openbaton.nfvo.common.exceptions.*;
import org.project.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/ns-records")
public class RestNetworkServiceRecord {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NetworkServiceRecordManagement networkServiceRecordManagement;

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return NetworkServiceRecord: the Network Service Descriptor filled with
	 *         id and values from core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public NetworkServiceRecord create(
			@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) throws InterruptedException, ExecutionException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
			return networkServiceRecordManagement.onboard(networkServiceDescriptor);

	}

	@RequestMapping(value = "{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public NetworkServiceRecord create(@PathVariable("id") String id) throws InterruptedException, ExecutionException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
		return networkServiceRecordManagement.onboard(id);
	}


	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) throws VimException, InterruptedException, ExecutionException, NotFoundException{
		try {
			networkServiceRecordManagement.delete(id);
		} catch (WrongStatusException e) {
			e.printStackTrace();
			throw new StateException(id);
		}
	}

	/**
	 * This operation returns the list of Network Service Descriptor (NSD)
	 *
	 * @return List<NetworkServiceRecord>: the list of Network Service
	 *         Descriptor stored
	 */

	@RequestMapping(method = RequestMethod.GET)
	public List<NetworkServiceRecord> findAll() {
		return networkServiceRecordManagement.query();
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor selected
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public NetworkServiceRecord findById(@PathVariable("id") String id) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		return nsr;
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 *
	 * @param networkServiceRecord
	 *            : the Network Service Descriptor to be updated
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public NetworkServiceRecord update(
			@RequestBody @Valid NetworkServiceRecord networkServiceRecord,
			@PathVariable("id") String id) {
		return networkServiceRecordManagement.update(networkServiceRecord, id);
	}

	/**
	 * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<VirtualNetworkFunctionDescriptor>: The List of
	 *         VirtualNetworkFunctionDescriptor into NSD
	 */
	@RequestMapping(value = "{id}/vnfrecords", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Set<VirtualNetworkFunctionRecord> getVirtualNetworkFunctionRecord(
			@PathVariable("id") String id) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		return nsr.getVnfr();
	}

	@RequestMapping(value = "{id}/vnfrecords/{id_vnf}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
			@PathVariable("id") String id, @PathVariable("id_vnf") String id_vnf) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		return findVNF(nsd.getVnfr(), id_vnf);
	}

	@RequestMapping(value = "{id}/vnfrecords/{id_vnf}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteVirtualNetworkFunctionDescriptor(
			@PathVariable("id") String id, @PathVariable("id_vnf") String id_vnf) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		VirtualNetworkFunctionRecord nRecord = findVNF(nsr.getVnfr(), id_vnf);
		nsr.getVnfr().remove(nRecord);
	}

	@RequestMapping(value = "{id}/vnfrecords/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public VirtualNetworkFunctionRecord postVNFR(
			@RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
			@PathVariable("id") String id) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		nsd.getVnfr().add(vnfRecord);
		networkServiceRecordManagement.update(nsd, id);
		return vnfRecord;
	}

	@RequestMapping(value = "{id}/vnfrecords/{id_vnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VirtualNetworkFunctionRecord updateVNF(
			@RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
			@PathVariable("id") String id, @PathVariable("id_vnf") String id_vnf) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		nsd.getVnfr().add(vnfRecord);
		networkServiceRecordManagement.update(nsd, id);
		return vnfRecord;
	}

	/**
	 * Returns the list of VNFDependency into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<VNFDependency>: The List of VNFDependency into NSD
	 */

	@RequestMapping(value = "{id}/vnfdependencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Set<VNFRecordDependency> getVNFDependencies(@PathVariable("id") String id) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		return nsd.getVnf_dependency();
	}

	@RequestMapping(value = "{id}/vnfdependencies/{id_vnfr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VNFRecordDependency getVNFDependency(@PathVariable("id") String id,
												@PathVariable("id_vnfr") String id_vnfr) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		return findVNFD(nsr.getVnf_dependency(), id_vnfr);
	}

	@RequestMapping(value = "{id}/vnfdependencies/{id_vnfd}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteVNFDependency(@PathVariable("id") String id,
			@PathVariable("id_vnfd") String id_vnfd) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		VNFRecordDependency vnfDependency = findVNFD(nsd.getVnf_dependency(), id_vnfd);
		nsd.getVnf_dependency().remove(vnfDependency);
	}

	@RequestMapping(value = "{id}/vnfdependencies/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public VNFRecordDependency postVNFDependency(
			@RequestBody @Valid VNFRecordDependency vnfDependency,
			@PathVariable("id") String id) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		nsr.getVnf_dependency().add(vnfDependency);
		networkServiceRecordManagement.update(nsr, id);
		return vnfDependency;
	}

	@RequestMapping(value = "{id}/vnfdependencies/{id_vnfd}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VNFRecordDependency updateVNFD(
			@RequestBody @Valid VNFRecordDependency vnfDependency,
			@PathVariable("id") String id,
			@PathVariable("id_vnfd") String id_vnfd) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		nsr.getVnf_dependency().add(vnfDependency);
		networkServiceRecordManagement.update(nsr, id);
		return vnfDependency;
	}

	/**
	 * Returns the list of PhysicalNetworkFunctionRecord into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<PhysicalNetworkFunctionRecord>: The List of
	 *         PhysicalNetworkFunctionRecord into NSD
	 */
	@RequestMapping(value = "{id}/pnfrecords", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Set<PhysicalNetworkFunctionRecord> getPhysicalNetworkFunctionRecord(
			@PathVariable("id") String id) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		return nsr.getPnfr();
	}

	/**
	 * Returns the PhysicalNetworkFunctionRecord
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         selected
	 */

	@RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public PhysicalNetworkFunctionRecord getPhysicalNetworkFunctionRecord(
			@PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		return findPNFD(nsd.getPnfr(), id_pnf);
	}

	/**
	 * Deletes the PhysicalNetworkFunctionRecord with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 */
	@RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePhysicalNetworkFunctionRecord(
			@PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) {
		NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
		PhysicalNetworkFunctionRecord pDescriptor = findPNFD(nsr.getPnfr(),
				id_pnf);
		nsr.getVnfr().remove(pDescriptor);
	}

	/**
	 * Stores the PhysicalNetworkFunctionRecord
	 *
	 * @param pDescriptor
	 *            : The PhysicalNetworkFunctionRecord to be stored
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         stored
	 */
	@RequestMapping(value = "{id}/pnfrecords/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public PhysicalNetworkFunctionRecord postPhysicalNetworkFunctionRecord(
			@RequestBody @Valid PhysicalNetworkFunctionRecord pDescriptor,
			@PathVariable("id") String id) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		nsd.getPnfr().add(pDescriptor);
		networkServiceRecordManagement.update(nsd, id);
		return pDescriptor;
	}

	/**
	 * Edits the PhysicalNetworkFunctionRecord
	 *
	 * @param pRecord
	 *            : The PhysicalNetworkFunctionRecord to be edited
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         edited
	 */
	@RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public PhysicalNetworkFunctionRecord updatePNFD(
			@RequestBody @Valid PhysicalNetworkFunctionRecord pRecord,
			@PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) {
		NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
		nsd.getPnfr().add(pRecord);
		networkServiceRecordManagement.update(nsd, id);
		return pRecord;
	}

	// TODO The Rest of the classes

	private PhysicalNetworkFunctionRecord findPNFD(Collection<PhysicalNetworkFunctionRecord> listPNFR, String id_pnf) {
		for (PhysicalNetworkFunctionRecord pRecord : listPNFR) {
			if (pRecord.getId().equals(id_pnf)) {
				return pRecord;
			}
		}
		throw new PNFDNotFoundException(id_pnf);
	}

	private VNFRecordDependency findVNFD(Collection<VNFRecordDependency> vnf_dependency, String id_vnfd) {
		for (VNFRecordDependency vnfDependency : vnf_dependency) {
			if (vnfDependency.getId().equals(id_vnfd)) {
				return vnfDependency;
			}
		}
		throw new VNFDependencyNotFoundException(id_vnfd);
	}

	private VirtualNetworkFunctionRecord findVNF(
			Collection<VirtualNetworkFunctionRecord> listVNF, String id_vnf) {
		for (VirtualNetworkFunctionRecord vnfRecord : listVNF) {
			if (vnfRecord.getId().equals(id_vnf)) {
				return vnfRecord;
			}
		}
		throw new VNFDNotFoundException(id_vnf);
	}



}
