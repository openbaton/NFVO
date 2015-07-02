package org.project.openbaton.nfvo.core.cli.command;

import org.project.openbaton.nfvo.sdk.Requestor;
import org.project.openbaton.nfvo.sdk.api.exception.SDKException;
import org.project.openbaton.nfvo.sdk.api.rest.VirtualLinkRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OpenBaton VirtualLinkDescriptor-related commands implementation using the spring-shell library.
 */
@Component
public class VirtualLink implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Adds a new VirtualLinkDescriptor to the repository
	 *
	 * @param virtualLinkDescriptor
	 *            : VirtualLinkDescriptor to add
	 * @return VirtualLinkDescriptor: The VirtualLinkDescriptor filled with values from the core
	 */
	@CliCommand(value = "virtualLinkDescriptor create", help = "Adds a new virtualLinkDescriptor to the virtualLinkDescriptor repository")
	public String create(
            @CliOption(key = { "virtualLinkDescriptorFile" }, mandatory = true, help = "The virtualLinkDescriptor json file") final File virtualLinkDescriptor) {
		try {
			VirtualLinkRequest virtualLinkRequest = Requestor.getVirtualLinkRequest();
			return "VLD CREATED: " + virtualLinkRequest.create(virtualLinkDescriptor);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VLD NOT CREATED";
		}
	}

	/**
	 * Removes the VirtualLinkDescriptor from the repository
	 *
	 * @param id: The VirtualLinkDescriptor's id to be deleted
	 */
	@CliCommand(value = "virtualLinkDescriptor delete", help = "Removes the virtualLinkDescriptor from the virtualLinkDescriptor repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The virtualLinkDescriptor id") final String id) {
		try {
			VirtualLinkRequest virtualLinkRequest = Requestor.getVirtualLinkRequest();
			virtualLinkRequest.delete(id);
			return  "VLD DELETED";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VLD NOT DELETED";
		}
	}

	/**
	 * Returns the VirtualLinkDescriptor selected by id
	 * @param id: The VirtualLinkDescriptor's id selected
	 * @return Datacenter: The VirtualLinkDescriptor selected
	 */
	@CliCommand(value = "virtualLinkDescriptor find", help = "Returns the virtualLinkDescriptor selected by id, or all if no id is given")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The virtualLinkDescriptor id") final String id) {
		try {
			VirtualLinkRequest virtualLinkRequest = Requestor.getVirtualLinkRequest();
			if (id != null) {
				return "FOUND VLD: " + virtualLinkRequest.findById(id);
			} else {
				return "FOUND VLDs: " + virtualLinkRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VLD NOT FOUND";
		}
	}

	/**
	 * This operation updates the VirtualLinkDescriptor
	 *
	 * @param virtualLinkDescriptor
	 *            : the new VirtualLinkDescriptor to be updated to
	 * @param id
	 *            : the id of the old VirtualLinkDescriptor
	 * @return VirtualLinkDescriptor: the VirtualLinkDescriptor updated
	 */
	@CliCommand(value = "virtualLinkDescriptor update", help = "Updates the virtualLinkDescriptor")
	public String update(
            @CliOption(key = { "virtualLinkDescriptorFile" }, mandatory = true, help = "The virtualLinkDescriptor json file") final File virtualLinkDescriptor,
            @CliOption(key = { "id" }, mandatory = true, help = "The virtualLinkDescriptor id") final String id) {
		try {
			VirtualLinkRequest virtualLinkRequest = Requestor.getVirtualLinkRequest();
			return "VLD UPDATED: " + virtualLinkRequest.update(virtualLinkDescriptor, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VLD NOT UPDATED";
		}
	}

}
