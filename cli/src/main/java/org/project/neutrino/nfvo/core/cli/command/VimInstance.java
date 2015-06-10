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
 * OpenBaton viminstance(datacenter)-related commands implementation using the spring-shell library.
 */
@Component
public class VimInstance implements CommandMarker {
	
	private static Logger log = LoggerFactory.getLogger("CLInterface");
	
	@Autowired
	private ConfigurableApplicationContext context;

	/**
	 * Adds a new datacenter to the datacenter repository
	 *
	 * @param datacenter
	 *            : Image to add
	 * @return datacenter: The datacenter filled with values from the core
	 */
	@CliCommand(value = "viminstance create", help = "Adds a new datacenter to the datacenter repository")
	public String create(
            @CliOption(key = { "datacenterFile" }, mandatory = true, help = "The image id to find.") final File datacenter) {
		return "IMAGE CREATED";
	}

	/**
	 * Removes the Datacenter from the Datacenter repository
	 *
	 * @param id: The Datacenter's id to be deleted
	 */
	@CliCommand(value = "viminstance delete", help = "Removes the Datacenter from the Datacenter repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The image id to find.") final String id) {
		return "IMAGE CREATED";
	}

	/**
	 * Returns the list of the Datacenters available
	 * @return List<Datacenter>: The List of Datacenters available
	 */
	@CliCommand(value = "viminstance find all", help = "Returns the list of the Datacenters available")
	public String findAll() {
		return "IMAGE RESULTS";
	}

	/**
	 * Returns the Datacenter selected by id
	 * @param id: The Datacenter's id selected
	 * @return Datacenter: The Datacenter selected
	 */
	@CliCommand(value = "viminstance find", help = "Returns the Datacenter selected by id")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The image id to find.") final String id) {
		return "IMAGE RESULT";
	}

	/**
	 * This operation updates the Datacenter
	 *
	 * @param datacenter
	 *            : the new datacenter to be updated to
	 * @param id
	 *            : the id of the old datacenter
	 * @return VimInstance: the VimInstance updated
	 */
	@CliCommand(value = "viminstance update", help = "Updates the Datacenter")
	public String update(
            @CliOption(key = { "datacenterFile" }, mandatory = true, help = "The image to find.") final File datacenter,
            @CliOption(key = { "id" }, mandatory = true, help = "The image id to find.") final String id) {
		return "IMAGE UPDATED";
	}

}
