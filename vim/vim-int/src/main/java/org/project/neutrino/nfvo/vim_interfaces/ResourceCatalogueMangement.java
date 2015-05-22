package org.project.neutrino.nfvo.vim_interfaces;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourceCatalogueMangement {
	
	/**
	 * This operation allows retrieving the list of catalogued virtualised 
	 * resources, and/or a specific catalogued resource on which the 
	 * consumer is allowed to perform subsequent operations.
	 */
	void query();
	
	/**
	 * This operation provides change notifications on virtualised resources 
	 * catalogues managed by the producer functional block.
	 */
	void notifyChange();
}
