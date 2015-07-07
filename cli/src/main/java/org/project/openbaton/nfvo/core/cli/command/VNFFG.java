package org.project.openbaton.nfvo.core.cli.command;


import com.google.gson.Gson;
import org.project.openbaton.nfvo.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
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
 * OpenBaton VNFFG-related commands implementation using the spring-shell library.
 */
@Component
public class VNFFG implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private NFVORequestor requestor = new NFVORequestor("1");
	private AbstractRestAgent<VNFForwardingGraphDescriptor> vNFFGRequest;
	private Gson mapper = new Gson();

	@PostConstruct
	private void init(){
		vNFFGRequest = requestor.getVNFFGAgent();
	}

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
			return "VNFFG CREATED: " + vNFFGRequest.create(mapper.<VNFForwardingGraphDescriptor>fromJson(new InputStreamReader(new FileInputStream(vnfForwardingGraphDescriptor)), VNFForwardingGraphDescriptor.class));
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFG NOT CREATED";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
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
			if (id != null) {
				return "FOUND VNFFG: " + vNFFGRequest.findById(id);
			} else {
				return "FOUND VNFFGs: " + vNFFGRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NO VNFFG FOUND";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
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
            return "VNFFG UPDATED: " + vNFFGRequest.update(mapper.<VNFForwardingGraphDescriptor>fromJson(new InputStreamReader(new FileInputStream(vnfForwardingGraphDescriptor)), VNFForwardingGraphDescriptor.class), id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFFG NOT UPDATED";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
