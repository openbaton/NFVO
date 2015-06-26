package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import java.io.File;

/**
 * OpenBaton VirtualLink -related api requester.
 */
public class VirtualLinkRequest extends Request {

	/**
	 * Create a VirtualLink requester with a given url path
	 *
	 * @param path
	 * 				the url path used for the api requests
	 */
	public VirtualLinkRequest(final String url) {
		super(url);
	}

	/**
	 * Adds a new VirtualLinkDescriptor to the repository
	 *
	 * @param virtualLinkDescriptor
	 *            : VirtualLinkDescriptor to add
	 * @return VirtualLinkDescriptor: The VirtualLinkDescriptor filled with values from the core
	 */
	public String create(final File virtualLinkDescriptor) throws SDKException {
		return requestPost(url, virtualLinkDescriptor);
	}

	/**
	 * Removes the VirtualLinkDescriptor from the repository
	 *
	 * @param id: The VirtualLinkDescriptor's id to be deleted
	 */
	public void delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		requestDelete(url);
	}

	/**
	 * Returns the list of the VirtualLinkDescriptor available
	 * @return List<VirtualLinkDescriptor>: The List of VirtualLinkDescriptor available
	 */
	public String findAll() throws SDKException {
		return requestGet(url);
	}

	/**
	 * Returns the VirtualLinkDescriptor selected by id
	 * @param id: The VirtualLinkDescriptor's id selected
	 * @return Datacenter: The VirtualLinkDescriptor selected
	 */
	public String findById(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return requestGet(url);
	}

	/**
	 * This operation updates the VirtualLinkDescriptor
	 *
	 * @param virtualLinkDescriptor
	 *            : the new VirtualLinkDescriptor to be updated to
	 * @param id
	 *            : the id of the old VirtualLinkDescriptor
	 * @return VirtualLinkDescriptor: the VirtualLinkDescriptor updated
	 */
	public String update(final File virtualLinkDescriptor, final String id) throws SDKException {
		String url = this.url + "/" + id;
		return requestPut(url, virtualLinkDescriptor);
	}

}
