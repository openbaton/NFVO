package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import java.io.File;

/**
 * OpenBaton configuration-related api requester.
 */
public class ConfigurationRequest extends Request {

	/**
	 * Create a configuration requester with a given url path
	 *
	 * @param url
	 * 				the url path used for the api requests
	 */
	public ConfigurationRequest(final String url) {
		super(url);
	}

	/**
	 * Adds a new Configuration to the Configurations repository
	 *
	 * @param configuration
	 * @return configuration
	 */
	public String create(final File configuration) throws SDKException {
		return post(url, configuration, "CONFIGURATION CREATED");
	}

	/**
	 * Removes the Configuration from the Configurations repository
	 *
	 * @param id
	 *            : the id of configuration to be removed
	 */
	public String delete(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return delete(url, "CONFIGURATION DELETED");
	}

	/**
	 * Returns the list of the Configurations available
	 *
	 * @return List<Configuration>: The list of Configurations available
	 */
	public String findAll() throws SDKException {
		return get(url, "FOUND CONFIGURATIONS");
	}

	/**
	 * Returns the Configuration selected by id
	 *
	 * @param id
	 *            : The id of the Configuration
	 * @return Configuration: The Configuration selected
	 */
	public String findById(final String id) throws SDKException {
		String url = this.url + "/" + id;
		return get(url, "FOUND CONFIGURATION");
	}

	/**
	 * Updates the Configuration
	 *
	 * @param configuration
	 *            : The Configuration to be updated
	 * @param id
	 *            : The id of the Configuration
	 * @return Configuration The Configuration updated
	 */
	public String update(final File configuration, final String id) throws SDKException {
		String url = this.url + "/" + id;
		return put(url, configuration, "CONFIGURATION UPDATED");
	}

}
