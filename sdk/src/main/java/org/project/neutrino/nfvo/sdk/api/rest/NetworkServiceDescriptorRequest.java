package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import java.io.File;

/**
 * OpenBaton network-service-descriptor-related api requester.
 */
public class NetworkServiceDescriptorRequest extends Request {

	/**
	 * Create a NetworkServiceDescriptor requester with a given url path
	 *
	 * @param url
	 * 				the url path used for the api requests
	 */
	public NetworkServiceDescriptorRequest(final String url) {
		super(url);
	}

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return networkServiceDescriptor: the Network Service Descriptor filled
	 *         with id and values from core
	 */
	public String create(final File networkServiceDescriptor) throws SDKException {
		return requestPost(url, networkServiceDescriptor);
	}

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	public void delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		requestDelete(url);
	}

	/**
	 * This operation returns the list of Network Service Descriptor (NSD)
	 *
	 * @return List<NetworkServiceDescriptor>: the list of Network Service
	 *         Descriptor stored
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
	 * @return NetworkServiceDescriptor: the Network Service Descriptor selected
	 */
	public String findById(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return requestGet(url);
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
	public String update(final File networkServiceDescriptor, final String id) throws SDKException {
		String url = this.url + "/" + id;
		return requestPut(url, networkServiceDescriptor);
	}

	/**
	 * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<VirtualNetworkFunctionDescriptor>: The List of
	 *         VirtualNetworkFunctionDescriptor into NSD
	 */
	public String getVirtualNetworkFunctionDescriptors(final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors";
		return requestGetWithStatusAccepted(url);
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionDescriptor(final String id, final String id_vfn) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/" + id_vfn;
		return requestGetWithStatusAccepted(url);
	}

	/**
	 *
	 */
	public void deleteVirtualNetworkFunctionDescriptors(final String id, final String id_vfn) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/" + id_vfn;
		requestDelete(url);
	}

	/**
	 *
	 */
	public String postVNFD(final File virtualNetworkFunctionDescriptor, final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/";
		return requestPost(url, virtualNetworkFunctionDescriptor);
	}

	/**
	 *
	 */
	public String updateVNF(final File virtualNetworkFunctionDescriptor, final String id, final String id_vfn) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/" + id_vfn;
		return requestPut(url, virtualNetworkFunctionDescriptor);
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
	public String getVNFDependency(final String id, final String id_vnfd) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfd;
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
	 * Returns the list of PhysicalNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<PhysicalNetworkFunctionDescriptor>: The List of
	 *         PhysicalNetworkFunctionDescriptor into NSD
	 *
	 */
	public String getPhysicalNetworkFunctionDescriptors(final String id) throws SDKException {
		String url = this.url + "/" + id + "/pnfdescriptors";
		return requestGetWithStatusAccepted(url);
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
	public String getPhysicalNetworkFunctionDescriptor(final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfdescriptors" + "/" + id_pnf;
		return requestGetWithStatusAccepted(url);
	}

	/**
	 * Deletes the PhysicalNetworkFunctionDescriptor with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 */
	public void deletePhysicalNetworkFunctionDescriptor(final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfdescriptors" + "/" + id_pnf;
		requestDelete(url);
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
	public String postPhysicalNetworkFunctionDescriptor(final File pnf, final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfdescriptors" + "/" + id_pnf;
		return requestPost(url, pnf);
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
	public String updatePNFD(final File pnf, final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfdescriptors" + "/" + id_pnf;
		return requestPut(url, pnf);
	}

	/**
	 * Returns the Security into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security of PhysicalNetworkFunctionDescriptor into
	 *         NSD
	 */
	public String getSecurities(final String id) throws SDKException {
		String url = this.url + "/" + id + "/security";
		return requestGetWithStatusAccepted(url);
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
	public String getSecurity(final String id, final String id_s) throws SDKException {
		String url = this.url + "/" + id + "/security" + "/" + id_s;
		return requestGetWithStatusAccepted(url);
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
	public void deleteSecurity(final String id, final String id_s) throws SDKException {
		String url = this.url + "/" + id + "/security" + "/" + id_s;
		requestDelete(url);
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
	public String postSecurity(final File security, final String id) throws SDKException {
		String url = this.url + "/" + id + "/security" + "/";
		return requestPost(url, security);
	}

	/**
	 *
	 */
	public String updateSecurity(final File security, final String id, final String id_s) throws SDKException {
		String url = this.url + "/" + id + "/security" + "/" + id_s;
		return requestPut(url, security);
	}

	/**
	 *
	 */
	public String createRecord(final File networkServiceDescriptor) throws SDKException {
		String url = this.url + "/records";
		return requestPost(url, networkServiceDescriptor);
	}
}
