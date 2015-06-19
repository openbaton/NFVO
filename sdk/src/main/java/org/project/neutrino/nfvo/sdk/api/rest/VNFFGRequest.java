package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import java.io.File;

/**
 * OpenBaton VNFFG-related api requester.
 */
public class VNFFGRequest extends Request {

	/**
	 * Create a VNFFG requester with a given url path
	 *
	 * @param path
	 * 				the url path used for the api requests
	 */
	public VNFFGRequest(final String url) {
		super(url);
	}

	/**
	 * Adds a new VNF software VNFFG to the vnfForwardingGraphDescriptor repository
	 *
	 * @param vnfForwardingGraphDescriptor
	 *            : VNFFG to add
	 * @return vnfForwardingGraphDescriptor: The vnfForwardingGraphDescriptor filled with values from the core
	 */
	public String create(final File vnfForwardingGraphDescriptor) throws SDKException {
		return post(url, vnfForwardingGraphDescriptor, "VNF SOFTWARE VNFFG CREATED");
	}

	/**
	 * Removes the VNF software VNFFG from the VNFFG repository
	 *
	 * @param id
	 *            : The VNFFG's id to be deleted
	 */
	public String delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return delete(url, "NF SOFTWARE VNFFG DELETED");
	}

	/**
	 * Returns the list of the VNFFGs available
	 *
	 * @return List<VNFForwardingGraphDescriptor>: The list of VNFFGs available
	 */
	public String findAll() throws SDKException {
		return get(url, "FOUND NF SOFTWARE VNFFGS");
	}

	/**
	 * Returns the VNFFG selected by id
	 *
	 * @param id
	 *            : The id of the VNFFG
	 * @return vnfForwardingGraphDescriptor: The VNFFG selected
	 */
	public String findById(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return get(url, "FOUND NF SOFTWARE VNFFG");
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
	public String update(final File vnfForwardingGraphDescriptor, final String id) throws SDKException {
		String url = this.url + "/" + id;
		return put(url, vnfForwardingGraphDescriptor, "NF SOFTWARE VNFFG UPDATED");
	}

}
