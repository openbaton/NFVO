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
		return requestPost(url, networkServiceDescriptor);
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
	public void delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		requestDelete(url);
	}

    /**
     * Returns the set of the Network Service Records available
     *
     * @return Set<NetworkServiceRecord>: The set of NetworkServiceRecords available
     */
	public String findAll() throws SDKException {
		return requestGet(url);
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
		return requestGet(url);
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
		return requestPut(url, networkServiceRecord);
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionRecords(final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords";
		return requestGetWithStatusAccepted(url);
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionRecord(final String id, final String id_vnf) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/" + id_vnf;
		return requestGetWithStatusAccepted(url);
	}

	/**
	 *
	 */
	public void deleteVirtualNetworkFunctionDescriptor(final String id, final String id_vnf) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/" + id_vnf;
		requestDelete(url);
	}

	/**
	 *
	 */
	public String postVNFR(final File networkServiceRecord, final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/";
		return requestPost(url, networkServiceRecord);
	}

	/**
	 *
	 */
	public String updateVNF(final File networkServiceRecord, final String id, final String id_vnf) throws SDKException {
		String url = this.url + "/" + id + "/vnfrecords" + "/" + id_vnf;
		return requestPut(url, networkServiceRecord);
	}

	/**
	 *
	 */
	public String getVNFDependencies(final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies";
		return requestGetWithStatusAccepted(url);
	}

	/**
	 *
	 */
	public String getVNFDependency(final String id, final String id_vnfr) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfr;
		return requestGetWithStatusAccepted(url);
	}

	/**
	 *
	 */
	public void deleteVNFDependency(final String id, final String id_vnfd) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfd;
		requestDelete(url);
	}

	/**
	 *
	 */
	public String postVNFDependency(final File vnfDependency, final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/";
		return requestPost(url, vnfDependency);
	}

	/**
	 *
	 */
	public String updateVNFD(final File vnfDependency, final String id, final String id_vnfd) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfd;
		return requestPut(url, vnfDependency);
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
		return requestGetWithStatusAccepted(url);
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
		return requestGetWithStatusAccepted(url);
	}

	/**
	 * Deletes the PhysicalNetworkFunctionRecord with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionRecord id
	 */
	public void deletePhysicalNetworkFunctionRecord(final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfrecords" + "/" + id_pnf;
		requestDelete(url);
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
		return requestPost(url, physicalNetworkFunctionRecord);
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
		return requestPut(url, physicalNetworkFunctionRecord);
	}

}
