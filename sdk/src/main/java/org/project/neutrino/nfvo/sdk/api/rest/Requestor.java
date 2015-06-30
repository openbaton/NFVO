package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.util.PropertyReader;

/**
 * OpenBaton api requestor. Can be extended with security features to provide instances only only to granted requestors.
 * The Class is implemented in a static way to avoid any dependencies to spring and to create a corresponding small lib size.
 */
public final class Requestor {

	// sdk.api.properties path as string
	private static final String MAIN_PROPERTIES_FILE = "application.properties";
	private static final String SDK_PROPERTIES_FILE = "sdk.api.properties";

	// get the url configuration from
	private static final PropertyReader propertyReader = new PropertyReader(MAIN_PROPERTIES_FILE, SDK_PROPERTIES_FILE);

	// create the requester here, maybe shift this to a manager
	private static final ConfigurationRequest configurationRequest = new ConfigurationRequest(propertyReader.getRestConfigurationUrl());
	private static final ImageRequest imageRequest = new ImageRequest(propertyReader.getRestImageUrl());
	private static final NetworkServiceDescriptorRequest networkServiceDescriptorRequest = new NetworkServiceDescriptorRequest(propertyReader.getRestNetworkServiceDescriptorUrl());
	private static final NetworkServiceRecordRequest networkServiceRecordRequest = new NetworkServiceRecordRequest(propertyReader.getRestNetworkServiceRecordUrl());
	private static final VimInstanceRequest vimInstanceRequest = new VimInstanceRequest(propertyReader.getRestVimInstanceUrl());
	private static final VirtualLinkRequest virtualLinkRequest = new VirtualLinkRequest(propertyReader.getRestVirtualLinkUrl());
	private static final VNFFGRequest vNFFGRequest = new VNFFGRequest(propertyReader.getRestVNFFGUrl());

//	TODO
//	private static final VirtualLinkRequest virtualLinkRequest = new VirtualLinkRequest(propertyReader.getRestVirtualLinkPath());

	/**
	 * create a "static class" with a private constructor
	 */
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

	/**
	 * Gets the virtualLink requester
	 *
	 * @return virtualLinkRequest: The (final) static virtualLink requester
	 */
	public static VirtualLinkRequest getVirtualLinkRequest() {
		return virtualLinkRequest;
	}

	/**
	 * Gets the VNFFG requester
	 *
	 * @return vNFFGRequest: The (final) static vNFFG requester
	 */
	public static VNFFGRequest getVNFFGRequest() {
		return vNFFGRequest;
	}

}
