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
		return post(url, networkServiceDescriptor, "NETWORKSERVICEDESCRIPTOR CREATED");
	}

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	public String delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return delete(url, "NETWORKSERVICEDESCRIPTOR DELETED");
	}

	/**
	 * This operation returns the list of Network Service Descriptor (NSD)
	 *
	 * @return List<NetworkServiceDescriptor>: the list of Network Service
	 *         Descriptor stored
	 */
	public String findAll() throws SDKException {
		return get(url, "FOUND NETWORKSERVICEDESCRIPTORS");
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
		return get(url, "FOUND NETWORKSERVICEDESCRIPTOR");
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
		return put(url, networkServiceDescriptor, "NETWORKSERVICEDESCRIPTOR UPDATED");
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
		return getWithStatusAccepted(url, "FOUND VIRTUALNETWORKSERVICEDESCRIPTORS");
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionDescriptor(final String id, final String id_vfn) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/" + id_vfn;
		return getWithStatusAccepted(url, "FOUND VIRTUALNETWORKSERVICEDESCRIPTOR");
	}

	/**
	 *
	 */
	public String deleteVirtualNetworkFunctionDescriptors(final String id, final String id_vfn) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/" + id_vfn;
		return delete(url, "DELETED VIRTUALNETWORKSERVICEDESCRIPTORS");
	}

	/**
	 *
	 */
	public String postVNFD(final File virtualNetworkFunctionDescriptor, final String id) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/";
		return post(url, virtualNetworkFunctionDescriptor, "VIRTUALNETWORKSERVICEDESCRIPTORS CREATED");
	}

	/**
	 *
	 */
	public String updateVNF(final File virtualNetworkFunctionDescriptor, final String id, final String id_vfn) throws SDKException {
		String url = this.url + "/" + id + "/vnfdescriptors" + "/" + id_vfn;
		return put(url, virtualNetworkFunctionDescriptor, "VIRTUALNETWORKSERVICEDESCRIPTOR UPDATED");
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
	public String getVNFDependency(final String id, final String id_vnfd) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfd;
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
		return post(url, vnfDependency, "CREATED VNFDEPENDENCY");
	}

	/**
	 *
	 */
	public String updateVNFD(final File vnfDependency, final String id, final String id_vnfd) throws SDKException {
		String url = this.url + "/" + id + "/vnfdependencies" + "/" + id_vnfd;
		return put(url, vnfDependency, "UPDATED VNFDEPENDENCY");
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
		return getWithStatusAccepted(url, "FOUND PNFDESCRIPTORS");
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
		return getWithStatusAccepted(url, "FOUND PNFDESCRIPTOR");
	}

	/**
	 * Deletes the PhysicalNetworkFunctionDescriptor with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 */
	public String deletePhysicalNetworkFunctionDescriptor(final String id, final String id_pnf) throws SDKException {
		String url = this.url + "/" + id + "/pnfdescriptors" + "/" + id_pnf;
		return delete(url, "DELETED PNFDESCRIPTOR");
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
		return post(url, pnf, "CREATED PNFDESCRIPTOR");
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
		return put(url, pnf, "UPDATED PNFDESCRIPTOR");
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
		return getWithStatusAccepted(url, "FOUND SECURITIES");
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
		return getWithStatusAccepted(url, "FOUND SECURITY");
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
	public String deleteSecurity(final String id, final String id_s) throws SDKException {
		String url = this.url + "/" + id + "/security" + "/" + id_s;
		return delete(url, "DELETED SECURITY");
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
		return post(url, security, "CREATED SECURITY");
	}

	/**
	 *
	 */
	public String updateSecurity(final File security, final String id, final String id_s) throws SDKException {
		String url = this.url + "/" + id + "/security" + "/" + id_s;
		return put(url, security, "UPDATED SECURITY");
	}

	/**
	 *
	 */
	public String createRecord(final File networkServiceDescriptor) throws SDKException {
		String url = this.url + "/records";
		return post(url, networkServiceDescriptor, "CREATED RECORD");
	}
}
