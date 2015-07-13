package org.project.openbaton.common.vnfm_sdk.interfaces;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFFaultManagement {

	/**
	 * This operation allows collecting VNF application-layer fault information.
	 */
	void getFaultInformation();
	
	/**
	 * This operation allows providing application-layer fault notifications.
	 */
	void notifyFault();
}
