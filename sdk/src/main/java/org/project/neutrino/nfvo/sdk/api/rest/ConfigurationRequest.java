package org.project.neutrino.nfvo.sdk.api.rest;

import java.io.File;

/**
 * OpenBaton configuration-related api requester.
 */
public class ConfigurationRequest {

	/**
	 * Adds a new Configuration to the Configurations repository
	 *
	 * @param configuration
	 * @return configuration
	 */
	public String create(final File configuration) {
		return "IMAGE CREATED";
	}

	/**
	 * Removes the Configuration from the Configurations repository
	 *
	 * @param id
	 *            : the id of configuration to be removed
	 */
	public String delete(final String id) {
		return "IMAGE CREATED";
	}

	/**
	 * Returns the list of the Configurations available
	 *
	 * @return List<Configuration>: The list of Configurations available
	 */
	public String findAll() {
		return "IMAGE RESULTS";
	}

	/**
	 * Returns the Configuration selected by id
	 *
	 * @param id
	 *            : The id of the Configuration
	 * @return Configuration: The Configuration selected
	 */
	public String findById(final String id) {
		return "IMAGE RESULT";
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
	public String update(final File configuration, final String id) {
		return "IMAGE UPDATED";
	}

}
