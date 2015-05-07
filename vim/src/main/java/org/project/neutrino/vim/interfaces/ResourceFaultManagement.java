package org.project.neutrino.vim.interfaces;

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
