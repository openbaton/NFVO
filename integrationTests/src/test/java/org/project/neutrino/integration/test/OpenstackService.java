/* Tiziano Cecamore - 2015*/

package org.project.neutrino.integration.test;

public class OpenstackService implements CloudService {

	// public static final String KEYSTONE_AUTH_URL =
	// "http://172.25.27.16:35357/v2.0";
	// public static final String KEYSTONE_ENDPOINT =
	// "http://172.25.27.16:35357/v2.0";
	// public static final String TENANT_NAME = "admin";

	public String getProvider() {
		// TODO Auto-generated method stub
		return "openstack-glance"; // PROVIDER
	}

	public String getIdentity() {
		// TODO Auto-generated method stub
		return "admin"; // KEYSTONE_USERNAME
	}

	public String getCredential() {
		// TODO Auto-generated method stub
		return "pass"; // KEYSTONE_PASSWORD
	}

	public String getEndpoint() {
		// TODO Auto-generated method stub
		return "http://192.168.41.45:5000/v2.0"; // NOVA_ENDPOINT
	}

}
