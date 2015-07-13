package org.project.openbaton.nfvo.core.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.common.catalogue.mano.descriptor.VirtualLinkDescriptor;
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
 * OpenBaton VirtualLinkDescriptor-related commands implementation using the spring-shell library.
 */
@Component
public class VirtualLink implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private NFVORequestor requestor = new NFVORequestor("1");
	private AbstractRestAgent<VirtualLinkDescriptor> virtualLinkRequest;
	private Gson mapper = new Gson();

	@PostConstruct
	private void init(){
		virtualLinkRequest = requestor.getVirtualLinkAgent();
	}

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
			return "VLD CREATED: " + virtualLinkRequest.create(mapper.<VirtualLinkDescriptor>fromJson(new InputStreamReader(new FileInputStream(virtualLinkDescriptor)), VirtualLinkDescriptor.class));
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VLD NOT CREATED";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
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
			if (id != null) {
				return "FOUND VLD: " + virtualLinkRequest.findById(id);
			} else {
				return "FOUND VLDs: " + virtualLinkRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VLD NOT FOUND";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
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
			return "VLD UPDATED: " + virtualLinkRequest.update(mapper.<VirtualLinkDescriptor>fromJson(new InputStreamReader(new FileInputStream(virtualLinkDescriptor)), VirtualLinkDescriptor.class), id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VLD NOT UPDATED";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
