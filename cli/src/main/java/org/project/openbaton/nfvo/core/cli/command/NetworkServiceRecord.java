package org.project.openbaton.nfvo.core.cli.command;

import org.project.openbaton.nfvo.sdk.Requestor;
import org.project.openbaton.nfvo.sdk.api.exception.SDKException;
import org.project.openbaton.nfvo.sdk.api.rest.NetworkServiceRecordRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "CREATED NSR: " + networkServiceRecordRequest.create(networkServiceDescriptor);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NSR NOT CREATED";
		}
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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "CREATED NSR: " + networkServiceRecordRequest.create(id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NSR NOT CREATED";
		}
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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			networkServiceRecordRequest.delete(id);
			return "DELETED NSR";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NSR NOT DELETED";
		}
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor selected
	 */
	@CliCommand(value = "networkServiceRecord find", help = "Returns the Network Service Record selected by id, or all if no id is given")
	public String findById(
            @CliOption(key = { "id" }, mandatory = false, help = "The networkServiceDescriptor id") final String id) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			if (id != null) {
				return "FOUND NSR: " + networkServiceRecordRequest.findById(id);
			} else {
				return "FOUND NSRs: " + networkServiceRecordRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NO NSR FOUND";
		}
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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "UPDATED NSR: " + networkServiceRecordRequest.update(networkServiceRecord, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NSR NOT UPDATED";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVirtualNetworkFunctionRecords", help = "TODO")
	public String getVirtualNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "FOUND VNFRECORDs: " + networkServiceRecordRequest.getVirtualNetworkFunctionRecords(id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFRECORD NOT FOUND";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVirtualNetworkFunctionRecord", help = "TODO")
	public String getVirtualNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "FOUND VNFRECORD: " + networkServiceRecordRequest.getVirtualNetworkFunctionRecord(id, id_vnf);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFRECORD NOT FOUND";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord deleteVirtualNetworkFunctionDescriptor", help = "TODO")
	public String deleteVirtualNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			networkServiceRecordRequest.deleteVirtualNetworkFunctionDescriptor(id, id_vnf);
			return "DELETED VNFRECORD";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFRECORD NOT DELETED";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord postVNFR", help = "TODO")
	public String postVNFR(
			@CliOption(key = { "networkServiceRecordFile" }, mandatory = true, help = "The NetworkServiceRecord json file") final File networkServiceRecord,
			@CliOption(key = { "id" }, mandatory = true, help = "TODO") final String id) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "FOUND VNFRECORDs: " + networkServiceRecordRequest.postVNFR(networkServiceRecord, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFRECORD NOT FOUND";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord updateVNF", help = "TODO")
	public String updateVNF(
			@CliOption(key = { "networkServiceRecordFile" }, mandatory = true, help = "The NetworkServiceRecord json file") final File networkServiceRecord,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "UPDATED VNFRECORD: " + networkServiceRecordRequest.updateVNF(networkServiceRecord, id, id_vnf);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFRECORD NOT UPDATED";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVNFDependencies", help = "TODO")
	public String getVNFDependencies(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "FOUND VNFDEPENDENCIES: " + networkServiceRecordRequest.getVNFDependencies(id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFDEPENDENCY NOT FOUND";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVNFDependency", help = "TODO")
	public String getVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfr" }, mandatory = true, help = "TODO") final String id_vnfr) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "FOUND VNFDEPENDENCY: " + networkServiceRecordRequest.getVNFDependency(id, id_vnfr);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFDEPENDENCY NOT FOUND";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord deleteVNFDependency", help = "TODO")
	public String deleteVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			networkServiceRecordRequest.deleteVNFDependency(id, id_vnfd);
			return "DELETED VNFDEPENDENCY";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFDEPENDENCY NOT DELETED";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord postVNFDependency", help = "TODO")
	public String postVNFDependency(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "The VNFDependency json file") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "CREATED VNFDEPENDENCY: " + networkServiceRecordRequest.postVNFDependency(vnfDependency, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFDEPENDENCY NOT CREATED";
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord updateVNFD", help = "TODO")
	public String updateVNFD(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "The VNFDependency json file") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "UPDATED VNFDEPENDENCY" + networkServiceRecordRequest.updateVNFD(vnfDependency, id, id_vnfd);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "VNFDEPENDENCY NOT UPDATED";
		}
	}

	/**
	 * Returns the set of PhysicalNetworkFunctionRecord into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Set<PhysicalNetworkFunctionRecord>: The Set of
	 *         PhysicalNetworkFunctionRecord into NSD
	 */
	@CliCommand(value = "networkServiceRecord getPhysicalNetworkFunctionRecords", help = "Returns the set of PhysicalNetworkFunctionRecords")
	public String getPhysicalNetworkFunctionRecords(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "FOUND PNFRECORDs: " + networkServiceRecordRequest.getPhysicalNetworkFunctionRecords(id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "PNFRECORD NOT FOUND";
		}
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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "FOUND PNFRECORD: " + networkServiceRecordRequest.getPhysicalNetworkFunctionRecord(id, id_pnf);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "PNFRECORD NOT FOUND";
		}
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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			networkServiceRecordRequest.deletePhysicalNetworkFunctionRecord(id, id_pnf);
			return "DELETED PNFRECORD";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "PNFRECORD NOT DELETED";
		}
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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "CREATED PNFRECORD: " + networkServiceRecordRequest.postPhysicalNetworkFunctionRecord(physicalNetworkFunctionRecord, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "PNFRECORD NOT CREATED";
		}
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
		try {
			NetworkServiceRecordRequest networkServiceRecordRequest = Requestor.getNetworkServiceRecordRequest();
			return "UPDATED PNFRECORD: " + networkServiceRecordRequest.updatePNFD(physicalNetworkFunctionRecord, id, id_pnf);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "PNFRECORD NOT UPDATED";
		}
	}

}
