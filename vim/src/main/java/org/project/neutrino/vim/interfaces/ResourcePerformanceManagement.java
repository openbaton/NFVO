package org.project.neutrino.vim.interfaces;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourcePerformanceManagement {
	
	/**
	 * This operation allows collecting performance measurement results 
	 * generated on virtualised resources.
	 */
	void getMeasurementResults();
	
	/**
	 * This operation allows providing notifications with performance
	 * measurement results on virtualised resources.
	 */
	void notifyResults();
}
