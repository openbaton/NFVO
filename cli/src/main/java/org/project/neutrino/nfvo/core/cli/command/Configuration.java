package org.project.neutrino.nfvo.core.cli.command;

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
	
	@Autowired
	private ConfigurableApplicationContext context;

	/**
	 * Adds a new Configuration to the Configurations repository
	 *
	 * @param configuration
	 * @return configuration
	 */
	@CliCommand(value = "configuration create", help = "Adds a new VNF software Image to the image repository")
	public String create(
            @CliOption(key = { "configurationFile" }, mandatory = true, help = "The configuration json file") final File configuration) {
		return "IMAGE CREATED";
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
		return "IMAGE CREATED";
	}

	/**
	 * Returns the list of the Configurations available
	 *
	 * @return List<Configuration>: The list of Configurations available
	 */
	@CliCommand(value = "configuration find all", help = "Returns the list of the Configurations available")
	public String findAll() {
		return "IMAGE RESULTS";
	}

	/**
	 * Returns the Configuration selected by id
	 *
	 * @param id
	 *            : The id of the Configuration
	 * @return Configuration: The Configuration selected
	 */
	@CliCommand(value = "configuration find", help = "Returns the Configuration selected by id")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The configuration id to find.") final String id) {
		return "IMAGE RESULT";
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
		return "IMAGE UPDATED";
	}

}
