package org.project.openbaton.common.vnfm_sdk.interfaces;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFPerformanceManagement {
	
	/**
	 * This operation allows collecting performance
	 * measurement results generated on resources.
	 */
	void getPerformanceMeasurementResults();

	/**
	 * This operation allows providing notifications with
	 * application-layer performance measurement results.
	 */
	void notifyMeasurementResults();
}
