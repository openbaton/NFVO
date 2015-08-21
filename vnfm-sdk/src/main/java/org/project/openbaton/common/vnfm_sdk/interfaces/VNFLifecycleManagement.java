package org.project.openbaton.common.vnfm_sdk.interfaces;


import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.CoreMessage;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFLifecycleManagement {
	
	/**
	 * This operation allows creating a VNF instance.
	 * @param virtualNetworkFunctionRecord
	 */
	CoreMessage instantiate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);
	
	/**
	 * This operation allows retrieving 
	 * VNF instance state and attributes.
	 */
	void query();
	
	/**
	 * This operation allows scaling 
	 * (out/in, up/down) a VNF instance.
	 */
	void scale();
	
	/**
	 * This operation allows verifying if 
	 * the VNF instantiation is possible.
	 */
	void checkInstantiationFeasibility();
	
	/**
	 * This operation allows verifying if 
	 * the VNF instantiation is possible.
	 */
	void heal();
	
	/**
	 * This operation allows applying a minor/limited 
	 * software update (e.g. patch) to a VNF instance.
	 */
	void updateSoftware();
	
	/**
	 * This operation allows making structural changes 
	 * (e.g. configuration, topology, behavior, 
	 * redundancy model) to a VNF instance.
	 * @param virtualNetworkFunctionRecord
	 * @param dependency
	 */
	
	CoreMessage modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFRecordDependency dependency);
	
	/**
	 * This operation allows deploying a new 
	 * software release to a VNF instance.
	 */
	void upgradeSoftware();
	
	/**
	 * This operation allows terminating gracefully
	 * or forcefully a previously created VNF instance.
	 * @param virtualNetworkFunctionRecord
	 */
	CoreMessage terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);
}
