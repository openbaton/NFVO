package org.project.neutrino.nfvo.core.cli.command;

import org.project.neutrino.nfvo.sdk.api.rest.Requestor;
import org.project.neutrino.nfvo.sdk.api.rest.VimInstanceRequest;
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
 * OpenBaton viminstance(datacenter)-related commands implementation using the spring-shell library.
 */
@Component
public class VimInstance implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Adds a new datacenter to the datacenter repository
	 *
	 * @param datacenter
	 *            : Image to add
	 * @return datacenter: The datacenter filled with values from the core
	 */
	@CliCommand(value = "viminstance create", help = "Adds a new datacenter to the datacenter repository")
	public String create(
            @CliOption(key = { "datacenterFile" }, mandatory = true, help = "The viminstance json file") final File datacenter) {
		try {
			VimInstanceRequest vimInstanceRequest = Requestor.getVimInstanceRequest();
			return vimInstanceRequest.create(datacenter);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VIMINSTANCE NOT CREATED";
		}
	}

	/**
	 * Removes the Datacenter from the Datacenter repository
	 *
	 * @param id: The Datacenter's id to be deleted
	 */
	@CliCommand(value = "viminstance delete", help = "Removes the Datacenter from the Datacenter repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
        try {
            VimInstanceRequest vimInstanceRequest = Requestor.getVimInstanceRequest();
            return vimInstanceRequest.delete(id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VIMINSTANCE NOT DELETED";
        }
	}

	/**
	 * Returns the Datacenter selected by id
	 * @param id: The Datacenter's id selected
	 * @return Datacenter: The Datacenter selected
	 */
	@CliCommand(value = "viminstance find", help = "Returns the Datacenter selected by id" )
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
        try {
            VimInstanceRequest vimInstanceRequest = Requestor.getVimInstanceRequest();
            if (id != null) {
                return vimInstanceRequest.findById(id);
            } else {
                return vimInstanceRequest.findAll();
            }
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NO VIMINSTANCE FOUND";
        }
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
            @CliOption(key = { "datacenterFile" }, mandatory = true, help = "The viminstance json file") final File datacenter,
            @CliOption(key = { "id" }, mandatory = true, help = "The viminstance id") final String id) {
        try {
            VimInstanceRequest vimInstanceRequest = Requestor.getVimInstanceRequest();
            return vimInstanceRequest.update(datacenter, id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VIMINSTANCE NOT UPDATED";
        }
	}

}
