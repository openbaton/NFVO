/* Tiziano Cecamore - 2015*/
package org.project.neutrino.integration.test;

public interface CloudService {

	public String getProvider();

	public String getIdentity();

	public String getCredential();

	public String getEndpoint();

}
