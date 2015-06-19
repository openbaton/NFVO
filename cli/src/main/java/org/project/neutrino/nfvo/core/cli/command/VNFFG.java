package org.project.neutrino.nfvo.core.cli.command;

import org.project.neutrino.nfvo.sdk.api.rest.Requestor;
import org.project.neutrino.nfvo.sdk.api.rest.VNFFGRequest;
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
			return vNFFGRequest.create(vnfForwardingGraphDescriptor);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFORWARDINGGRAPHDESCRIPTOR NOT CREATED";
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
			return vNFFGRequest.delete(id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFORWARDINGGRAPHDESCRIPTOR NOT DELETED";
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
				return vNFFGRequest.findById(id);
			} else {
				return vNFFGRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NO VNFFORWARDINGGRAPHDESCRIPTOR FOUND";
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
            return vNFFGRequest.update(vnfForwardingGraphDescriptor, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFORWARDINGGRAPHDESCRIPTOR NOT UPDATED";
		}
	}

}
