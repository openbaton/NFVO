package org.project.neutrino.nfvo.core.interfaces;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFLifecycleOperationGranting {
	
	/**
	 * This operation allows requesting the permission to 
	 * perform a certain VNF lifecycle operation on a new 
	 * or existing VNF. The sub-type of lifecycle operation 
	 * is parameterized in the operation.
	 */
	void grantLifecycleOperation();
}
