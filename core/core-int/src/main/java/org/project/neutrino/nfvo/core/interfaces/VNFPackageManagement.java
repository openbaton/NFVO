package org.project.neutrino.nfvo.core.interfaces;

import java.util.List;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFPackageManagement {
	
	/**
	 * This operation allows submitting and 
	 * validating the VNF Package.
	 */
	void onboard();

	/**
	 * This operation allows disabling the 
	 * VNF Package, so that it is not 
	 * possible to instantiate any further.
	 */
	void disable();

	/**
	 * This operation allows enabling 
	 * the VNF Package.
	 */
	void enable();

	/**
	 * This operation allows updating 
	 * the VNF Package.
	 */
	void update();

	/**
	 * This operation is used to query 
	 * information on VNF Packages.
	 */
	List<String> query();

	/**
	 * This operation is used to remove a
	 * disabled VNF Package.
	 */
	void delete();
}
