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
 * OpenBaton image-related commands implementation using the spring-shell library.
 */
@Component
public class NetworkServiceRecord implements CommandMarker {
	
	private static Logger log = LoggerFactory.getLogger("CLInterface");
	
	@Autowired
	private ConfigurableApplicationContext context;

	/**
	 * Creates a Network Service Record
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return NetworkServiceRecord: the Network Service Descriptor filled with
	 *         id and values from core
	 */
	@CliCommand(value = "networkServiceRecord create", help = "submitting and validating a Network Service Descriptor (NSD), including any related VNFFGD and VLD")
	public String create(
            @CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor) {
		return "IMAGE CREATED";
	}

	/**
	 * Creates a Network Service Record
	 *
	 * @param id
	 *            : the Network Service Descriptor id to be created
	 * @return NetworkServiceRecord: the Network Service Descriptor filled with
	 *         id and values from core
	 */
	@CliCommand(value = "networkServiceRecord create", help = "submitting and validating a Network Service Descriptor (NSD), including any related VNFFGD and VLD")
	public String create(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE CREATED";
	}

	/**
     * Removes a Network Service Record
     *
     * @param id
     *            : The NetworkServiceRecord's id to be deleted
     */
	@CliCommand(value = "networkServiceRecord delete", help = "Removes the Network Service Record")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE CREATED";
	}

    /**
     * Returns the set of the Network Service Records available
     *
     * @return Set<NetworkServiceRecord>: The set of NetworkServiceRecords available
     */
	@CliCommand(value = "networkServiceRecord find all", help = "Returns the set of the Network Service Records available")
	public String findAll() {
		return "IMAGE RESULTS";
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor selected
	 */
	@CliCommand(value = "networkServiceRecord find", help = "Returns the Network Service Record selected by id")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
		return "IMAGE RESULT";
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 *
	 * @param networkServiceRecord
	 *            : the Network Service Descriptor to be updated
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor updated
	 */
	@CliCommand(value = "networkServiceRecord update", help = "Updates the Network Service Record")
	public String update(
            @CliOption(key = { "networkServiceRecordFile" }, mandatory = true, help = "The NetworkServiceRecord json file") final File networkServiceRecord,
            @CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVirtualNetworkFunctionRecord", help = "TODO")
	public String getVirtualNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVirtualNetworkFunctionRecord", help = "TODO")
	public String getVirtualNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord deleteVirtualNetworkFunctionDescriptor", help = "TODO")
	public String deleteVirtualNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord postVNFR", help = "TODO")
	public String postVNFR(
			@CliOption(key = { "networkServiceRecordFile" }, mandatory = true, help = "The NetworkServiceRecord json file") final File networkServiceRecord,
			@CliOption(key = { "id" }, mandatory = true, help = "TODO") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord updateVNF", help = "TODO")
	public String updateVNF(
			@CliOption(key = { "networkServiceRecordFile" }, mandatory = true, help = "The NetworkServiceRecord json file") final File networkServiceRecord,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVNFDependencies", help = "TODO")
	public String getVNFDependencies(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVNFDependency", help = "TODO")
	public String getVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfr" }, mandatory = true, help = "TODO") final String id_vnfr) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord deleteVNFDependency", help = "TODO")
	public String deleteVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord postVNFDependency", help = "TODO")
	public String postVNFDependency(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "The VNFDependency json file") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord updateVNFD", help = "TODO")
	public String updateVNFD(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "The VNFDependency json file") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 * Returns the set of PhysicalNetworkFunctionRecord into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Set<PhysicalNetworkFunctionRecord>: The Set of
	 *         PhysicalNetworkFunctionRecord into NSD
	 */
	@CliCommand(value = "networkServiceRecord getPhysicalNetworkFunctionRecord", help = "Returns the set of PhysicalNetworkFunctionRecords")
	public String getPhysicalNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 * Returns the PhysicalNetworkFunctionRecord
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         selected
	 */
	@CliCommand(value = "networkServiceRecord getPhysicalNetworkFunctionRecord", help = "Returns the PhysicalNetworkFunctionRecord")
	public String getPhysicalNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Deletes the PhysicalNetworkFunctionRecord with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 */
	@CliCommand(value = "networkServiceRecord deletePhysicalNetworkFunctionRecord", help = "Deletes the PhysicalNetworkFunctionRecord with the id_pnf")
	public String deletePhysicalNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Stores the PhysicalNetworkFunctionRecord
	 *
	 * @param physicalNetworkFunctionRecord
	 *            : The PhysicalNetworkFunctionRecord to be stored
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         stored
	 */
	@CliCommand(value = "networkServiceRecord postPhysicalNetworkFunctionRecord", help = "Stores the PhysicalNetworkFunctionRecord")
	public String postPhysicalNetworkFunctionRecord(
			@CliOption(key = { "physicalNetworkFunctionRecordFile" }, mandatory = true, help = "The physicalNetworkFunctionRecord json file") final File physicalNetworkFunctionRecord,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 * Edits the PhysicalNetworkFunctionRecord
	 *
	 * @param physicalNetworkFunctionRecord
	 *            : The PhysicalNetworkFunctionRecord to be edited
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
	 *         edited
	 */
	@CliCommand(value = "networkServiceRecord updatePNFD", help = "Edits the PhysicalNetworkFunctionRecord")
	public String updatePNFD(
			@CliOption(key = { "physicalNetworkFunctionRecordFile" }, mandatory = true, help = "The physicalNetworkFunctionRecord json file") final File physicalNetworkFunctionRecord,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
		return "IMAGE UPDATED";
	}

}
