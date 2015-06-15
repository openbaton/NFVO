package org.project.neutrino.nfvo.sdk.api.rest;

/**
 * OpenBaton api requestor. Can be extended with security features to provide instances only only to granted requestors.
 * The Class is implemented in a static way to avoid any dependencies to spring and to create a corresponding small lib size.
 */
public final class Requestor {

	// create the requester here
	private static final ConfigurationRequest configurationRequest = new ConfigurationRequest();
	private static final ImageRequest imageRequest = new ImageRequest();
	private static final NetworkServiceDescriptorRequest networkServiceDescriptorRequest = new NetworkServiceDescriptorRequest();
	private static final NetworkServiceRecordRequest networkServiceRecordRequest = new NetworkServiceRecordRequest();
	private static final VimInstanceRequest vimInstanceRequest = new VimInstanceRequest();


	// create a "static class" with a private constructor
	private Requestor() {};

	/**
	 * Gets the configuration requester
	 *
	 * @return configurationRequest: The (final) static configuration requester
	 */
	public static ConfigurationRequest getConfigurationRequest() {
		return configurationRequest;
	}

	/**
	 * Gets the image requester
	 *
	 * @return image: The (final) static image requester
	 */
	public static ImageRequest getImageRequest() {
		return imageRequest;
	}

	/**
	 * Gets the networkServiceDescriptor requester
	 *
	 * @return networkServiceDescriptorRequest: The (final) static networkServiceDescriptor requester
	 */
	public static NetworkServiceDescriptorRequest getNetworkServiceDescriptorRequest() {
		return networkServiceDescriptorRequest;
	}
	/**
	 * Gets the networkServiceRecord requester
	 *
	 * @return networkServiceRecordRequest: The (final) static networkServiceRecord requester
	 */

	public static NetworkServiceRecordRequest getNetworkServiceRecordRequest() {
		return networkServiceRecordRequest;
	}

	/**
	 * Gets the vimInstance requester
	 *
	 * @return vimInstanceRequest: The (final) static vimInstance requester
	 */
	public static VimInstanceRequest getVimInstanceRequest() {
		return vimInstanceRequest;
	}

}
