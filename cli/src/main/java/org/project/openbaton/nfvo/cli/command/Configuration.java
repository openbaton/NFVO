package org.project.openbaton.nfvo.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.nfvo.api.RestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * OpenBaton configuration-related commands implementation using the spring-shell library.
 */
@Component
@Order
public class Configuration implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Gson mapper = new Gson();

	@Autowired
	private RestConfiguration configurationRequest;

	private org.project.openbaton.catalogue.nfvo.Configuration getObject(File file) throws FileNotFoundException {
		return mapper.<org.project.openbaton.catalogue.nfvo.Configuration>fromJson(new InputStreamReader(new FileInputStream(file)), org.project.openbaton.catalogue.nfvo.Configuration.class);
	}

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
			return "CONFIGURATION CREATED: " + configurationRequest.create(getObject(configuration));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "CONFIGURATION NOT CREATED: (" + e.getMessage() + " )";
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
			configurationRequest.delete(id);
			return "CONFIGURATION DELETED";
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
			if (id != null) {
				return "FOUND CONFIGURATION: " + configurationRequest.findById(id);
			} else {
				return "FOUND CONFIGURATIONS: " + configurationRequest.findAll();
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
			return "CONFIGURATION UPDATED: " + configurationRequest.update(getObject(configuration), id);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "CONFIGURATION NOT UPDATED: (" + e.getMessage() + " )";
		}
	}

}
