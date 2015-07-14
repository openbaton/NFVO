package org.project.openbaton.nfvo.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.common.catalogue.mano.common.VNFRecordDependency;
import org.project.openbaton.common.catalogue.mano.record.PhysicalNetworkFunctionRecord;
import org.project.openbaton.common.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.api.RestNetworkServiceRecord;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * OpenBaton image-related commands implementation using the spring-shell library.
 */
@Component
public class NetworkServiceRecord implements CommandMarker {

	private Logger log = LoggerFactory.getLogger(this.getClass());


	private Gson mapper = new Gson();

	@Autowired
	private RestNetworkServiceRecord networkServiceRecordAgent;

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
		try{
			return "CREATED NSR: " + networkServiceRecordAgent.create(this.getObject(networkServiceDescriptor, org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor.class));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (NotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (ExecutionException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (VimException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (NamingException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (JMSException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (VimDriverException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (BadFormatException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		}
	}

	private <T> T getObject(File networkServiceDescriptor, Class<T> clazz) throws FileNotFoundException {
		return mapper.fromJson(new InputStreamReader(new FileInputStream(networkServiceDescriptor)), clazz);
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
			return "CREATED NSR: " + networkServiceRecordAgent.create(id);
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (ExecutionException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (NamingException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (VimException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (JMSException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (NotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (BadFormatException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (VimDriverException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		}
	}

	/**
	 * Removes a Network Service Record
	 *
	 * @param id
	 *            : The NetworkServiceRecord's id to be deleted
	 */
	@CliCommand(value = "networkServiceRecord delete", help = "Removes the Network Service Record")
	public String delete(@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceRecord id") final String id) {
		try {
			networkServiceRecordAgent.delete(id);
			return "DELETED NSR";
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (ExecutionException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (VimException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (NamingException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (NotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		} catch (JMSException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
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
		if (id != null) {
			return "FOUND NSR: " + networkServiceRecordAgent.findById(id);
		} else {
			return "FOUND NSRs: " + networkServiceRecordAgent.findAll();
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
			return "UPDATED NSR: " + networkServiceRecordAgent.update(getObject(networkServiceRecord, org.project.openbaton.common.catalogue.mano.record.NetworkServiceRecord.class), id);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVirtualNetworkFunctionRecords", help = "TODO")
	public String getVirtualNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		return "FOUND VNFRECORDs: " + networkServiceRecordAgent.getVirtualNetworkFunctionRecord(id);
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVirtualNetworkFunctionRecord", help = "TODO")
	public String getVirtualNetworkFunctionRecord(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "The VirtualNetworkFunctionRecord id") final String id_vnf) {
		return "FOUND VNFRECORD: " + networkServiceRecordAgent.getVirtualNetworkFunctionRecord(id, id_vnf);
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord deleteVirtualNetworkFunctionDescriptor", help = "TODO")
	public String deleteVirtualNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		networkServiceRecordAgent.deleteVirtualNetworkFunctionDescriptor(id, id_vnf);
		return "Deleted VirtualNetworkFunctionDescriptor with id: " + id_vnf;
	}

	/**
	 *
	 */
	@CliCommand(value = "VNFForwardingGraph postVNFR", help = "TODO")
	public String postVNFR(
			@CliOption(key = { "networkServiceRecordFile" }, mandatory = true, help = "The NetworkServiceRecord json file") final File VNFForwardingGraph,
			@CliOption(key = { "id" }, mandatory = true, help = "TODO") final String id) {
		throw new UnsupportedOperationException();
//		try {
//			return "FOUND VNFRECORDs: " + networkServiceRecordAgent.postVNFR(getObject(VNFForwardingGraph, VirtualNetworkFunctionDescriptor.class), id);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			log.error(e.getLocalizedMessage());
//			return e.getMessage();
//		}
	}

	/**
	 *
	 */
	@CliCommand(value = "virtualNetoworkFunctionRecord updateVNF", help = "TODO")
	public String updateVNF(
			@CliOption(key = { "networkServiceRecordFile" }, mandatory = true, help = "The NetworkServiceRecord json file") final File virtualNetoworkFunctionRecord,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnf" }, mandatory = true, help = "TODO") final String id_vnf) {
		try {
			return "UPDATED VNFRECORD: " + networkServiceRecordAgent.updateVNF(getObject(virtualNetoworkFunctionRecord, VirtualNetworkFunctionRecord.class), id, id_vnf);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		}
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVNFDependencies", help = "TODO")
	public String getVNFDependencies(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
			return "FOUND VNFDEPENDENCIES: " + networkServiceRecordAgent.getVNFDependencies(id);
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord getVNFDependency", help = "TODO")
	public String getVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfr" }, mandatory = true, help = "TODO") final String id_vnfr) {
			return "FOUND VNFDEPENDENCY: " + networkServiceRecordAgent.getVNFDependency(id, id_vnfr);
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord deleteVNFDependency", help = "TODO")
	public String deleteVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
		networkServiceRecordAgent.deleteVNFDependency(id, id_vnfd);
			return "DELETED VNFDEPENDENCY";
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceRecord postVNFDependency", help = "TODO")
	public String postVNFDependency(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "The VNFDependency json file") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The NetworkServiceRecord id") final String id) {
		try {
			return "CREATED VNFDEPENDENCY: " + networkServiceRecordAgent.postVNFDependency(getObject(vnfDependency, VNFRecordDependency.class), id);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
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
			return "UPDATED VNFDEPENDENCY" + networkServiceRecordAgent.updateVNFD(getObject(vnfDependency, VNFRecordDependency.class), id, id_vnfd);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
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
			return "FOUND PNFRECORDs: " + networkServiceRecordAgent.getPhysicalNetworkFunctionRecord(id);
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
			return "FOUND PNFRECORD: " + networkServiceRecordAgent.getPhysicalNetworkFunctionRecord(id, id_pnf);
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
			networkServiceRecordAgent.deletePhysicalNetworkFunctionRecord(id, id_pnf);
			return "DELETED PNFRECORD";
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
			return "CREATED PNFRECORD: " + networkServiceRecordAgent.postPhysicalNetworkFunctionRecord(getObject(physicalNetworkFunctionRecord, PhysicalNetworkFunctionRecord.class), id);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
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
			return "UPDATED PNFRECORD: " + networkServiceRecordAgent.updatePNFD(getObject(physicalNetworkFunctionRecord, PhysicalNetworkFunctionRecord.class), id, id_pnf);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			return e.getMessage();
		}
	}

}
