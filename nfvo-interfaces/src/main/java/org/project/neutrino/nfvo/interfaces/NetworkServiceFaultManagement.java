package org.project.neutrino.nfvo.interfaces;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkServiceFaultManagement {
	
	/**
	 * This operation allows providing 
	 * fault notifications on Network Services.
	 */
	void notifyFault();
	
	/**
	 * This operation allows collecting 
	 * Network Service fault information.
	 */
	void getFaultInformation();
}
