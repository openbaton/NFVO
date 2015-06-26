package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import java.io.File;

/**
 * OpenBaton viminstance(datacenter)-related api requester.
 */
public class VimInstanceRequest extends Request {

	/**
	 * Create a VimInstance requester with a given url path
	 *
	 * @param path
	 * 				the url path used for the api requests
	 */
	public VimInstanceRequest(final String url) {
		super(url);
	}

	/**
	 * Adds a new datacenter to the datacenter repository
	 *
	 * @param datacenter
	 *            : Image to add
	 * @return datacenter: The datacenter filled with values from the core
	 */
	public String create(final File datacenter) throws SDKException {
		return requestPost(url, datacenter);
	}

	/**
	 * Removes the Datacenter from the Datacenter repository
	 *
	 * @param id: The Datacenter's id to be deleted
	 */
	public void delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		requestDelete(url);
	}

	/**
	 * Returns the list of the Datacenters available
	 * @return List<Datacenter>: The List of Datacenters available
	 */
	public String findAll() throws SDKException {
		return requestGet(url);
	}

	/**
	 * Returns the Datacenter selected by id
	 * @param id: The Datacenter's id selected
	 * @return Datacenter: The Datacenter selected
	 */
	public String findById(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return requestGet(url);
	}

	/**
	 * This operation updates the Datacenter
	 *
	 * @param datacenter
	 *            : the new datacenter to be updated to
	 * @param id
	 *            : the id of the old datacenter
	 * @return VimInstance: the VimInstance updated
	 */
	public String update(final File datacenter, final String id) throws SDKException {
		String url = this.url + "/" + id;
		return requestPut(url, datacenter);
	}

}
