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
 * OpenBaton network-service-descriptor-related commands implementation using the spring-shell library.
 */
@Component
public class NetworkServiceDescriptor implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ConfigurableApplicationContext context;

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return networkServiceDescriptor: the Network Service Descriptor filled
	 *         with id and values from core
	 */
	@CliCommand(value = "networkServiceDescriptor create", help = "Submits and validates a new Network Service Descriptor (NSD)")
	public String create(
            @CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor) {
		return "IMAGE CREATED";
	}

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	@CliCommand(value = "networkServiceDescriptor delete", help = "Removes a disabled Network Service Descriptor (NSD)")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE CREATED";
	}

	/**
	 * This operation returns the list of Network Service Descriptor (NSD)
	 *
	 * @return List<NetworkServiceDescriptor>: the list of Network Service
	 *         Descriptor stored
	 */
	@CliCommand(value = "networkServiceDescriptor find all", help = "Returns the list of the Network Service Descriptors (NSDs) available")
	public String findAll() {
		return "IMAGE RESULTS";
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceDescriptor: the Network Service Descriptor selected
	 */
	@CliCommand(value = "networkServiceDescriptor find", help = "Returns the Network Service Descriptor (NSD) selected by id")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE RESULT";
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be updated
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return networkServiceDescriptor: the Network Service Descriptor updated
	 */
	@CliCommand(value = "networkServiceDescriptor update", help = "Updates he Network Service Descriptor (NSD)")
	public String update(
            @CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor,
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/////////////////////////////////////////////////////////////////

	/**
	 * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<VirtualNetworkFunctionDescriptor>: The List of
	 *         VirtualNetworkFunctionDescriptor into NSD
	 */
	@CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptors", help = "Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id")
	public String getVirtualNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptor", help = "TODO")
	public String getVirtualNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "TODO") final String id_vfn) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor deleteVirtualNetworkFunctionDescriptor", help = "TODO")
	public String getVirtualNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "TODO") final String id_vfn) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor postVNFD", help = "TODO")
	public String postVNFD(
			@CliOption(key = { "virtualNetworkFunctionDescriptorFile" }, mandatory = true, help = "TODO") final File virtualNetworkFunctionDescriptor,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor updateVNF", help = "TODO")
	public String updateVNF(
			@CliOption(key = { "virtualNetworkFunctionDescriptorFile" }, mandatory = true, help = "TODO") final File virtualNetworkFunctionDescriptor,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "TODO") final String id_vfn) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getVNFDependencies", help = "TODO")
	public String getVNFDependencies(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getVNFDependency", help = "TODO")
	public String getVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor deleteVNFDependency", help = "TODO")
	public String deleteVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor postVNFDependency", help = "TODO")
	public String postVNFDependency(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "TODO") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor updateVNFD", help = "TODO")
	public String updateVNFD(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "TODO") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 * Returns the list of PhysicalNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<PhysicalNetworkFunctionDescriptor>: The List of
	 *         PhysicalNetworkFunctionDescriptor into NSD
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptors", help = "TODO")
	public String getPhysicalNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 * Returns the PhysicalNetworkFunctionDescriptor
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor selected
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptor", help = "TODO")
	public String getPhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Deletes the PhysicalNetworkFunctionDescriptor with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 */
	@CliCommand(value = "networkServiceDescriptor deletePhysicalNetworkFunctionDescriptor", help = "TODO")
	public String deletePhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Stores the PhysicalNetworkFunctionDescriptor
	 *
	 * @param pnf
	 *            : The PhysicalNetworkFunctionDescriptor to be stored
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor stored
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor postPhysicalNetworkFunctionDescriptor", help = "TODO")
	public String postPhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "pnfFile" }, mandatory = true, help = "TODO") final File pnf,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Edits the PhysicalNetworkFunctionDescriptor
	 *
	 * @param pnf
	 *            : The PhysicalNetworkFunctionDescriptor to be edited
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor edited
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor updatePNFD", help = "TODO")
	public String updatePNFD(
			@CliOption(key = { "pnfFile" }, mandatory = true, help = "TODO") final File pnf,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Returns the Security into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security of PhysicalNetworkFunctionDescriptor into
	 *         NSD
	 */
	@CliCommand(value = "networkServiceDescriptor getSecurity", help = "TODO")
	public String getSecurity(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 * Returns the Security with the id_s
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_s
	 *            : The Security id
	 * @return Security: The Security selected by id_s
	 */
	@CliCommand(value = "networkServiceDescriptor getSecurity", help = "TODO")
	public String getSecurity(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "TODO") final String id_s) {
		return "IMAGE UPDATED";
	}

	/**
	 * Deletes the Security with the id_s
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_s
	 *            : The Security id
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor deleteSecurity", help = "TODO")
	public String deleteSecurity(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "TODO") final String id_s) {
		return "IMAGE UPDATED";
	}

	/**
	 * Stores the Security into NSD
	 *
	 * @param security
	 *            : The Security to be stored
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security stored
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor postSecurity", help = "TODO")
	public String postSecurity(
			@CliOption(key = { "securityFile" }, mandatory = true, help = "TODO") final File security,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor updateSecurity", help = "TODO")
	public String updateSecurity(
			@CliOption(key = { "securityFile" }, mandatory = true, help = "TODO") final File security,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "TODO") final String id_s) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor createRecord", help = "TODO")
	public String createRecord(
			@CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "TODO") final File networkServiceDescriptor) {
		return "IMAGE UPDATED";
	}
}
