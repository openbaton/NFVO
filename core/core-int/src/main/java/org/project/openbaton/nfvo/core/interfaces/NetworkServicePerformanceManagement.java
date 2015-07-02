package org.project.openbaton.nfvo.core.interfaces;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkServicePerformanceManagement {
	
	/**
	 * This operation allows collecting performance 
	 * measurement results generated on Network Services.
	 */
	void getPerformanceMeasurementResults();
	
	/**
	 * This operation allows providing performance 
	 * notifications on Network Services.
	 */
	void notifyPerformance();
}
