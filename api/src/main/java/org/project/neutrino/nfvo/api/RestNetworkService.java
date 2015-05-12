package org.project.neutrino.nfvo.api;

import java.util.List;

import javax.persistence.NoResultException;
import javax.validation.Valid;

import org.project.neutrino.nfvo.api.exceptions.NSDNotFoundException;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dbo
 *
 */
@RestController
@RequestMapping("/ns-descriptors")
public class RestNetworkService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;

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
}
