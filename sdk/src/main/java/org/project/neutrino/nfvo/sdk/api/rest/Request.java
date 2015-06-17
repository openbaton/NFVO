package org.project.neutrino.nfvo.sdk.api.rest;

/**
 * OpenBaton api request request abstraction for all requester. Shares common data and methods.
 */
public class Request {

	protected final String path;

	/**
	 * Create a request with a given url path
	 *
	 * @param path
	 * 				the url path used for the api requests
	 */
	public Request(final String path) {
		this.path = path;
	}
}
