package org.project.openbaton.nfvo.vim_interfaces.resource_management;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourceFaultManagement {

	/**
	 * This operation allows collecting 
	 * virtualised resource fault information.
	 */
	void getInformation();
	
	/**
	 * This operation allows providing fault 
	 * notifications on virtualised resources.
	 */
	void notifyInformation();
}
