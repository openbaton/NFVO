package org.project.openbaton.nfvo.core.interfaces;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkServiceLifecycleManagement {
	
	/**
	 * This operation allows instantiating a Network Service.
	 */
	void instantiate();

	/**
	 * This operation allows terminating a 
	 * Network Service instance.
	 * Graceful or forceful termination might be 
	 * possible based on input parameter.
	 */
	void terminate();

	/**
	 * This operation allows retrieving Network
	 * Service instance attributes.
	 */
	void query();

	/**
	 * This operation allows scaling a Network
	 * Service instance.
	 */
	void scale();

	/**
	 * This operation allows updating a Network
	 * Service instance.
	 */
	void update();

	/**
	 * This operation allows creating a new VNF
	 * Forwarding Graph instance for a given
	 * Network Service instance.
	 */
	void createVNFFG();

	/**
	 * This operation allows deleting an existing
	 * VNF Forwarding Graph instance within a
	 * Network Service instance.
	 */
	void deleteVNFFG();

	/**
	 * This operation allows retrieving VNFFG
	 * instance attributes.
	 */
	void queryVNFFG();

	/**
	 * This operation allows updating an existing
	 * VNF Forwarding Graph instance for a given
	 * Network Service instance.
	 */
	void updateVNFFG();

	/**
	 * This operation allows creating a new VL for
	 * a given Network Service instance.
	 */
	void createVL();

	/**
	 * This operation allows deleting an existing
	 * VL within a Network Service instance.
	 */
	void deleteVL();

	/**
	 * This operation allows updating an existing
	 * VL for a given Network Service instance.
	 */
	void updateVL();

	/**
	 * This operation allows retrieving VL instance
	 * attributes.
	 */
	void queryVL();
}
