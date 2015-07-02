package org.project.openbaton.nfvo.core.cli.command;


import org.project.openbaton.nfvo.sdk.Requestor;
import org.project.openbaton.nfvo.sdk.api.exception.SDKException;
import org.project.openbaton.nfvo.sdk.api.rest.VNFFGRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OpenBaton VNFFG-related commands implementation using the spring-shell library.
 */
@Component
public class VNFFG implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Adds a new VNF software VNFFG to the vnfForwardingGraphDescriptor repository
	 *
	 * @param vnfForwardingGraphDescriptor
	 *            : VNFFG to add
	 * @return vnfForwardingGraphDescriptor: The vnfForwardingGraphDescriptor filled with values from the core
	 */
	@CliCommand(value = "vnfForwardingGraphDescriptor create", help = "Adds a new vnfForwardingGraphDescriptor to the vnfForwardingGraphDescriptor repository")
	public String create(
            @CliOption(key = { "vnfForwardingGraphDescriptorFile" }, mandatory = true, help = "The vnfForwardingGraphDescriptor json file") final File vnfForwardingGraphDescriptor) {
		try {
			VNFFGRequest vNFFGRequest = Requestor.getVNFFGRequest();
			return "VNFFG CREATED: " + vNFFGRequest.create(vnfForwardingGraphDescriptor);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFG NOT CREATED";
		}
	}

	/**
	 * Removes the VNF software VNFFG from the VNFFG repository
	 *
	 * @param id
	 *            : The VNFFG's id to be deleted
	 */
	@CliCommand(value = "vnfForwardingGraphDescriptor delete", help = "Removes the vnfForwardingGraphDescriptor from the vnfForwardingGraphDescriptor repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The vnfForwardingGraphDescriptor id") final String id) {
		try {
            VNFFGRequest vNFFGRequest = Requestor.getVNFFGRequest();
			vNFFGRequest.delete(id);
			return "VNFFG DELETED";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFG NOT DELETED";
		}
	}

	/**
	 * Returns the VNFFG selected by id
	 *
	 * @param id
	 *            : The id of the VNFFG
	 * @return vnfForwardingGraphDescriptor: The VNFFG selected
	 */
	@CliCommand(value = "vnfForwardingGraphDescriptor find", help = "Returns the vnfForwardingGraphDescriptor selected by id, or all if no id is given")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The vnfForwardingGraphDescriptor id") final String id) {
		try {
            VNFFGRequest vNFFGRequest = Requestor.getVNFFGRequest();
			if (id != null) {
				return "FOUND VNFFG: " + vNFFGRequest.findById(id);
			} else {
				return "FOUND VNFFGs: " + vNFFGRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NO VNFFG FOUND";
		}
	}
	/**
	 * Updates the VNF software vnfForwardingGraphDescriptor
	 *
	 * @param vnfForwardingGraphDescriptor
	 *            : the VNF software vnfForwardingGraphDescriptor to be updated
	 * @param id
	 *            : the id of VNF software vnfForwardingGraphDescriptor
	 * @return networkServiceDescriptor: the VNF software vnfForwardingGraphDescriptor updated
	 */
	@CliCommand(value = "vnfForwardingGraphDescriptor update", help = "Updates the vnfForwardingGraphDescriptor")
	public String update(
            @CliOption(key = { "vnfForwardingGraphDescriptorFile" }, mandatory = true, help = "The vnfForwardingGraphDescriptor json file") final File vnfForwardingGraphDescriptor,
            @CliOption(key = { "id" }, mandatory = true, help = "The vnfForwardingGraphDescriptor id") final String id) {
		try {
            VNFFGRequest vNFFGRequest = Requestor.getVNFFGRequest();
            return "VNFFG UPDATED: " + vNFFGRequest.update(vnfForwardingGraphDescriptor, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFG NOT UPDATED";
		}
	}

}
