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
	 * Adds a new Configuration to the Configurations repository
	 * @param configuration
	 * @return configuration
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public Configuration create(@RequestBody @Valid Configuration configuration) {
		log.trace("Adding Configuration: " + configuration);
		log.debug("Adding Configuration");
		return configurationManagement.add(configuration);
	}

	/**
	 * Removes the Configuration from the Configurations repository
	 * @param id: the id of configuration to be removed
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) {
		log.debug("removing Configuration with id " + id);
		configurationManagement.delete(id);
	}

	/**
	 * Returns the list of the Configurations available
	 * @return List<Configuration>: The list of Configurations available
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<Configuration> findAll() {
		log.debug("Find all Configurations");
		return configurationManagement.query();
	}

	/**
	 * Returns the Configuration selected by id
	 * @param id: The id of the Configuration
	 * @return Configuration: The Configuration selected
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public Configuration findById(@PathVariable("id") String id) {
		log.debug("find Configuration with id " + id);
		Configuration configuration = configurationManagement.query(id);
		log.trace("Found Configuration: " + configuration);
		return configuration;
	}

	/**
	 * Updates the Configuration
	 * @param new_configuration:  The Configuration to be updated
	 * @param id: The id of the Configuration
	 * @return Configuration The Configuration updated
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
