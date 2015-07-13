package org.project.openbaton.nfvo.vim_interfaces.policy_management;

import org.project.openbaton.common.catalogue.mano.descriptor.Policy;

import java.util.List;

/**
 * Created by mpa on 30/04/15.
 */

public interface PolicyManagement {

	/**
	 * This operation allows defining policy rules include 
	 * conditions and actions.
	 */
	Policy create();

	/**
	 * This operation allows updating an existing policy.
	 */
	Policy update();

	/**
	 * This operation allows delete policy after being created.
	 */
	void delete();

	/**
	 * This operation allows querying about a particular policy 
	 * or a querying the list of available policies.
	 */
	List<Policy> query();

	/**
	 * This operation enables activating an available policy.
	 */
	void activate();
	
	/**
	 * This operation enables de-activating an active policy.
	 */
	void deactivate();
}
