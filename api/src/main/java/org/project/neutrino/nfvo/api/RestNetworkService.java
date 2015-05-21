package org.project.neutrino.nfvo.api;

import org.project.neutrino.nfvo.api.exceptions.NSDNotFoundException;
import org.project.neutrino.nfvo.catalogue.mano.common.Security;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDependency;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.persistence.NoResultException;
import javax.validation.Valid;

import java.util.List;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/ns-descriptors")
public class RestNetworkService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;
	@Autowired
	private NetworkServiceRecordManagement networkServiceRecordManagement;

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 * 
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return networkServiceDescriptor: the Network Service Descriptor filled
	 *         with id and values from core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public NetworkServiceDescriptor create(
			@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) {
		return networkServiceDescriptorManagement
				.onboard(networkServiceDescriptor);
	}

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 * 
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) {
		try {
			networkServiceDescriptorManagement.delete(id);
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);

		}
	}

	/**
	 * This operation returns the list of Network Service Descriptor (NSD)
	 * 
	 * @return List<NetworkServiceDescriptor>: the list of Network Service
	 *         Descriptor stored
	 */

	@RequestMapping(method = RequestMethod.GET)
	public List<NetworkServiceDescriptor> findAll() {

		return networkServiceDescriptorManagement.query();
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 * 
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceDescriptor: the Network Service Descriptor selected
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public NetworkServiceDescriptor findById(@PathVariable("id") String id) {
		NetworkServiceDescriptor nsd = null;
		try {
			nsd = networkServiceDescriptorManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsd;
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 * 
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be updated
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return networkServiceDescriptor: the Network Service Descriptor updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public NetworkServiceDescriptor update(
			@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
			@PathVariable("id") String id) {
		return networkServiceDescriptorManagement.update(
				networkServiceDescriptor, id);
	}

	@RequestMapping(value = "{id}/vnfdescriptors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<VirtualNetworkFunctionDescriptor> getVirtualNetworkFunctionDescriptors(
			@PathVariable("id") String id) {
		NetworkServiceDescriptor nsd = null;
		try {
			nsd = networkServiceDescriptorManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsd.getVnfd();
	}

	@RequestMapping(value = "{id}/vnfdependencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<VNFDependency> getVNFDependencies(@PathVariable("id") String id) {
		NetworkServiceDescriptor nsd = null;
		try {
			nsd = networkServiceDescriptorManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsd.getVnf_dependency();
	}
	
	@RequestMapping(value = "{id}/pnfdescriptors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<PhysicalNetworkFunctionDescriptor> getPhysicalNetworkFunctionDescriptors(@PathVariable("id") String id) {
		NetworkServiceDescriptor nsd = null;
		try {
			nsd = networkServiceDescriptorManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsd.getPnfd();
	}
	
	@RequestMapping(value = "{id}/security", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Security getNsd_security(@PathVariable("id") String id) {
		NetworkServiceDescriptor nsd = null;
		try {
			nsd = networkServiceDescriptorManagement.query(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsd.getNsd_security();
	}

	@RequestMapping(value = "/records", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	NetworkServiceRecord createRecord(@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) {
		try {
			return networkServiceRecordManagement.onboard(networkServiceDescriptor);
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;//TODO return error

	}
}
