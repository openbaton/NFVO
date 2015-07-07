package org.project.openbaton.nfvo.core.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.sdk.NFVORequestor;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.project.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * OpenBaton configuration-related commands implementation using the spring-shell library.
 */
@Component
public class Configuration implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private NFVORequestor requestor = new NFVORequestor("1");
	private AbstractRestAgent<org.project.openbaton.nfvo.catalogue.nfvo.Configuration> configurationRequest;
	private Gson mapper = new Gson();

	@PostConstruct
	private void init(){
		configurationRequest = requestor.getConfigurationAgent();
	}
	private org.project.openbaton.nfvo.catalogue.nfvo.Configuration getObject(File file) throws FileNotFoundException {
		return mapper.<org.project.openbaton.nfvo.catalogue.nfvo.Configuration>fromJson(new InputStreamReader(new FileInputStream(file)), org.project.openbaton.nfvo.catalogue.nfvo.Configuration.class);
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
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "CONFIGURATION NOT CREATED: (" + e.getMessage() + " )";
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
		try {
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
			if (id != null) {
				return "FOUND CONFIGURATION: " + configurationRequest.findById(id);
			} else {
				return "FOUND CONFIGURATIONS: " + configurationRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NO CONFIGURATION FOUND: (" + e.getMessage() + " )";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return "NO CONFIGURATION FOUND: (" + e.getMessage() + " )";
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
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "CONFIGURATION NOT UPDATED: (" + e.getMessage() + " )";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "CONFIGURATION NOT UPDATED: (" + e.getMessage() + " )";
		}
	}

}
