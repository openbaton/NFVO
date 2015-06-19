package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import java.io.File;

/**
 * OpenBaton image-related commands api requester.
 */
public class NetworkServiceRecordRequest extends Request {

	/**
	 * Create a NetworkServiceRecord requester with a given url path
	 *
	 * @param url
	 * 				the url path used for the api requests
	 */
	public NetworkServiceRecordRequest(final String url) {
		super(url);
	}

	/**
	 * Creates a Network Service Record
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return NetworkServiceRecord: the Network Service Descriptor filled with
	 *         id and values from core
	 */
	public String create(final File networkServiceDescriptor) throws SDKException {
		return post(url, networkServiceDescriptor, "NETWORKSERVICEDESCRIPTOR CREATED");
	}

	/**
	 * Creates a Network Service Record
	 *
	 * @param id
	 *            : the Network Service Descriptor id to be created
	 * @return NetworkServiceRecord: the Network Service Descriptor filled with
	 *         id and values from core
	 */
	public String create(final String id) throws SDKException {
		throw new SDKException("NOT IMPLEMENTED");
	}

	/**
     * Removes a Network Service Record
     *
     * @param id
     *            : The NetworkServiceRecord's id to be deleted
     */
	public String delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return delete(url, "NETWORKSERVICERECORD DELETED");
	}

    /**
     * Returns the set of the Network Service Records available
     *
     * @return Set<NetworkServiceRecord>: The set of NetworkServiceRecords available
     */
	public String findAll() throws SDKException {
		return get(url, "FOUND NETWORKSERVICERECORDS");
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceRecord: the Network Service Descriptor selected
	 */
	public String findById(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return get(url, "FOUND NETWORKSERVICERECORD");
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
	public String update(final File networkServiceRecord, final String id) throws SDKException {
		String url = this.url + "/" + id;
		return put(url, networkServiceRecord, "NETWORKSERVICERECORD UPDATED");
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionRecords(final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords";
		return getWithStatusAccepted(url, "FOUND VNFRECORDS");
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionRecord(final String id, final String id_vnf) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/" + id_vnf;
		return getWithStatusAccepted(url, "FOUND VNFRECORD");
	}

	/**
	 *
	 */
	public String deleteVirtualNetworkFunctionDescriptor(final String id, final String id_vnf) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/" + id_vnf;
		return delete(url, "DELETED VNFRECORDS");
	}

	/**
	 *
	 */
	public String postVNFR(final File networkServiceRecord, final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/";
		return post(url, networkServiceRecord, "VNFRECORDS CREATED");
	}

	/**
	 *
	 */
	public String updateVNF(final File networkServiceRecord, final String id, final String id_vnf) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/" + id_vnf;
		return put(url, networkServiceRecord, "VNFRECORDS UPDATED");
	}

	/**
	 *
	 */
	public String getVNFDependencies(final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies";
		return getWithStatusAccepted(url, "FOUND VNFDEPENDENCIES");
	}

	/**
	 *
	 */
	public String getVNFDependency(final String id, final String id_vnfr) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfr;
		return getWithStatusAccepted(url, "FOUND VNFDEPENDENCY");
	}

	/**
	 *
	 */
	public String deleteVNFDependency(final String id, final String id_vnfd) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfd;
		return delete(url, "DELETED VNFDEPENDENCY");
	}

	/**
	 *
	 */
	public String postVNFDependency(final File vnfDependency, final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/";
		return post(url, vnfDependency, "VNFDEPENDENCY CREATED");
	}

	/**
	 *
	 */
	public String updateVNFD(final File vnfDependency, final String id, final String id_vnfd) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfd;
		return put(url, vnfDependency, "VNFDEPENDENCY UPDATED");
	}

	/**
	 * Returns the set of PhysicalNetworkFunctionRecord into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Set<PhysicalNetworkFunctionRecord>: The Set of
	 *         PhysicalNetworkFunctionRecord into NSD
	 */
	public String getPhysicalNetworkFunctionRecords(final String id) throws SDKException {
		String url = this.url + "/" + id + "/pnfrecords";
		return getWithStatusAccepted(url, "FOUND PNFRECORDS");
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
	public String getPhysicalNetworkFunctionRecord(final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfrecords" + "/" + id_pnf;
		return getWithStatusAccepted(url, "FOUND PNFRECORDS");
	}

	/**
	 * Deletes the PhysicalNetworkFunctionRecord with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 */
	public String deletePhysicalNetworkFunctionRecord(final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfrecords" + "/" + id_pnf;
		return delete(url, "DELETED PNFRECORDS");
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
	public String postPhysicalNetworkFunctionRecord(final File physicalNetworkFunctionRecord, final String id) throws SDKException {
		String url = this.url + "/" + id + "/pnfrecords" + "/";
		return post(url, physicalNetworkFunctionRecord, "PNFRECORDS CREATED");
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
	public String updatePNFD(final File physicalNetworkFunctionRecord, final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfrecords" + "/" + id_pnf;
		return put(url, physicalNetworkFunctionRecord, "PNFRECORDS UPDATED");
	}

}
