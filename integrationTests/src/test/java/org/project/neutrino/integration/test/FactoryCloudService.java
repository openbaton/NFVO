/* Tiziano Cecamore - 2015*/
package org.project.neutrino.integration.test;

public class FactoryCloudService {

	public static String getProviderOpenstack() {
		return new OpenstackService().getProvider();
	}

	public static String getIdentityOpenstack() {
		return new OpenstackService().getIdentity();
	}

	public static String getCredentialOpenstack() {
		return new OpenstackService().getCredential();
	}

	public static String getEndpointOpenstack() {
		return new OpenstackService().getEndpoint();
	}

}
