package org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourceCapacityManagement {
	
	/**
	 * This operation allows querying the capacity usage of an NFVI-PoP. The operation 
	 * can be used to gather information at different levels, from specific virtualised 
	 * partition capacity usage, to total capacity availability in the NFVI-PoP.
	 */
	String query(String pop);
	
	/**
	 * This operation allows notifying about capacity changes in the NFVI-PoP.
	 */
	void notifyChanges(String notification);
	
}
