package org.project.neutrino.nfvo.api;

import org.project.neutrino.nfvo.catalogue.nfvo.Configuration;
import org.project.neutrino.nfvo.core.interfaces.ConfigurationManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author dbo
 *
 */
@RestController
@RequestMapping("/configurations")
public class RestConfiguration {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ConfigurationManagement configurationManagement;

	/**
	 * Adds a new VNF software Image to the configuration repository
	 * 
	 * @param configuration
	 *            : Image to add
	 * @return configuration: The configuration filled with values from the core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public Configuration create(@RequestBody @Valid Configuration configuration) {
		log.trace("Adding Configuration: " + configuration);
		log.debug("Adding Configuration");
		return configurationManagement.add(configuration);
	}

	/**
	 * Removes the VNF software Image from the Image repository
	 * 
	 * @param id
	 *            : The Image's id to be deleted
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) {
		log.debug("removing Configuration with id " + id);
		configurationManagement.delete(id);
	}

	/**
	 * Returns the list of the VNF software images available
	 * 
	 * @return List<Image>: The list of VNF software images available
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<Configuration> findAll() {
		log.debug("Find all Configurations");
		return configurationManagement.query();
	}

	/**
	 * This operation returns the VNF software image selected by id
	 * 
	 * @param id
	 *            : The id of the VNF software image
	 * @return image: The VNF software image selected
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public Configuration findById(@PathVariable("id") String id) {
		log.debug("find Configuration with id " + id);
		Configuration configuration = configurationManagement.query(id);
		log.trace("Found Configuration: " + configuration);
		return configuration;
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 * 
	 * @param new_configuration
	 *            : the new configuration to be updated to
	 * @param id
	 *            : the id of the old datacenter
	 * @return datacenter: the Configuration updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Configuration update(@RequestBody @Valid Configuration new_configuration,
			@PathVariable("id") String id) {
		log.trace("updating Configuration with id " + id + " with values: " + new_configuration);
		log.debug("updating Configuration with id " + id);
		return configurationManagement.update(new_configuration, id);
	}
}
