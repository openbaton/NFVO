package org.project.neutrino.nfvo.common.vnfm.interfaces;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFLifecycleChangeNotification {
	
	/**
	 * This operation allows providing notifications on state changes 
	 * of a VNF instance, related to the VNF Lifecycle.
	 */
	void NotifyChange();
}
