package org.project.neutrino.nfvo.core.cli.command;

import org.project.neutrino.nfvo.sdk.api.rest.Requestor;
import org.project.neutrino.nfvo.sdk.api.rest.ConfigurationRequest;
import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OpenBaton configuration-related commands implementation using the spring-shell library.
 */
@Component
public class Configuration implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Adds a new Configuration to the Configurations repository
	 *
	 * @param configuration
	 * @return configuration
	 */
	@CliCommand(value = "configuration create", help = "Adds a new VNF software Image to the image repository")
	public String create(
            @CliOption(key = { "configurationFile" }, mandatory = true, help = "The configuration json file") final File configuration) {
		try {
			ConfigurationRequest configurationRequest = Requestor.getConfigurationRequest();
			return "CONFIGURATION CREATED: " + configurationRequest.create(configuration);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "CONFIGURATION NOT CREATED";
		}
	}

	/**
	 * Removes the Configuration from the Configurations repository
	 *
	 * @param id
	 *            : the id of configuration to be removed
	 */
	@CliCommand(value = "configuration delete", help = "Removes the VNF software Image from the Configurations repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The configuration id") final String id) {
		try {
			ConfigurationRequest configurationRequest = Requestor.getConfigurationRequest();
			configurationRequest.delete(id);
			return "CONFIGURATION DELETED";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "CONFIGURATION NOT DELETED";
		}
	}

	/**
	 * Returns the Configuration selected by id
	 *
	 * @param id
	 *            : The id of the Configuration
	 * @return Configuration: The Configuration selected
	 */
	@CliCommand(value = "configuration find", help = "Returns the Configuration selected by id, or all if no id is given")
	public String findById(
            @CliOption(key = { "id" }, mandatory = false, help = "The configuration id to find.") final String id) {
		try {
			ConfigurationRequest configurationRequest = Requestor.getConfigurationRequest();
			if (id != null) {
				return "FOUND CONFIGURATION: " + configurationRequest.findById(id);
			} else {
				return "FOUND CONFIGURATIONS: " + configurationRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NO CONFIGURATION FOUND";
		}
	}

	/**
	 * Updates the Configuration
	 *
	 * @param configuration
	 *            : The Configuration to be updated
	 * @param id
	 *            : The id of the Configuration
	 * @return Configuration The Configuration updated
	 */
	@CliCommand(value = "configuration update", help = "Updates the Configuration")
	public String update(
            @CliOption(key = { "configurationFile" }, mandatory = true, help = "The configuration json file") final File configuration,
            @CliOption(key = { "id" }, mandatory = true, help = "The configuration id") final String id) {
		try {
			ConfigurationRequest configurationRequest = Requestor.getConfigurationRequest();
			return "CONFIGURATION UPDATED: " + configurationRequest.update(configuration, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "CONFIGURATION NOT UPDATED";
		}
	}

}
