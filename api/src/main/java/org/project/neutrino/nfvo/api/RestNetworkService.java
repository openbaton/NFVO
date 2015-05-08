package org.project.neutrino.nfvo.api;

import java.util.List;

import javax.persistence.NoResultException;
import javax.validation.Valid;

import org.project.neutrino.nfvo.api.exceptions.NSDNotFoundException;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ns-descriptors")
public class RestNetworkService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("NSDRepository")
	private GenericRepository<NetworkServiceDescriptor> nsdRepository;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	NetworkServiceDescriptor create(
			@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) {
		return nsdRepository.create(networkServiceDescriptor);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable("id") String id) {
		try {
			nsdRepository.remove(nsdRepository.find(id));
		} catch (NoResultException e) {
			log.error(e.getMessage());
			throw new NSDNotFoundException(id);

		}
	}

	@RequestMapping(method = RequestMethod.GET)
	List<NetworkServiceDescriptor> findAll() {
		return nsdRepository.findAll();
	}

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	NetworkServiceDescriptor findById(@PathVariable("id") String id) {
		NetworkServiceDescriptor nsd = null;
		try {
			nsd = nsdRepository.find(id);
		} catch (NoResultException e) {

			log.error(e.getMessage());
			throw new NSDNotFoundException(id);
		}
		return nsd;
	}

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	NetworkServiceDescriptor update(
			@RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) {
		return nsdRepository.merge(networkServiceDescriptor);
	}

}
