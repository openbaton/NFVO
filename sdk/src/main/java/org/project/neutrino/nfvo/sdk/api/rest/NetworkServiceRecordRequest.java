package org.project.neutrino.nfvo.sdk.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OpenBaton image-related commands api requester.
 */
@Component
public class NetworkServiceRecordRequest{
	
//	private static Logger log = LoggerFactory.getLogger("SDKApi");
	
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
	public String create(final File networkServiceDescriptor) {
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
	public String create(final String id) {
		return "IMAGE CREATED";
	}

	/**
     * Removes a Network Service Record
     *
     * @param id
     *            : The NetworkServiceRecord's id to be deleted
     */
	public String delete(final String id) {
		return "IMAGE CREATED";
	}

    /**
     * Returns the set of the Network Service Records available
     *
     * @return Set<NetworkServiceRecord>: The set of NetworkServiceRecords available
     */
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
	public String findById(final String id) {
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
	public String update(final File networkServiceRecord, final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionRecord(final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionRecord(final String id, final String id_vnf) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String deleteVirtualNetworkFunctionDescriptor(final String id, final String id_vnf) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String postVNFR(final File networkServiceRecord, final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String updateVNF(final File networkServiceRecord, final String id, final String id_vnf) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVNFDependencies(final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVNFDependency(final String id, final String id_vnfr) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String deleteVNFDependency(final String id, final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String postVNFDependency(final File vnfDependency, final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String updateVNFD(final File vnfDependency, final String id, final String id_vnfd) {
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
	public String getPhysicalNetworkFunctionRecord(final String id) {
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
	public String getPhysicalNetworkFunctionRecord(final String id, final String id_pnf) {
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
	public String deletePhysicalNetworkFunctionRecord(final String id, final String id_pnf) {
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
	public String postPhysicalNetworkFunctionRecord(final File physicalNetworkFunctionRecord, final String id) {
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
	public String updatePNFD(final File physicalNetworkFunctionRecord, final String id, final String id_pnf) {
		return "IMAGE UPDATED";
	}

}
