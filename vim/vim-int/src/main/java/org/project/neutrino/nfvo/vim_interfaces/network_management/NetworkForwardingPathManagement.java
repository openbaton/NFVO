package org.project.neutrino.nfvo.vim_interfaces.network_management;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkForwardingPath;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkForwardingPathManagement {
	
	/**
	 * This operation allows creating a Network Forwarding Path.
	 */
	NetworkForwardingPath create();

	/**
	 * This operation allows updating the information 
	 * associated with a Network Forwarding Path.
	 */
	NetworkForwardingPath update();

	/**
	 * This operation allows deleting a 
	 * Network Forwarding Path.
	 */
	void delete();

	/**
	 * This operation allows querying information about 
	 * a specified Network Forwarding Path instance.
	 */
	NetworkForwardingPath query();

	/**
	 * This operation allows providing information 
	 * about a Network Forwarding Path rule.
	 */
	void notifyInformation();
}
