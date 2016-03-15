/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.common.vnfm_sdk.interfaces;

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VimInstance;

import java.util.Collection;
import java.util.List;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFLifecycleManagement {
	
	/**
	 * This operation allows creating a VNF instance.
	 * @param virtualNetworkFunctionRecord
	 * @param scripts
	 * @param vimInstances
	 */

	VirtualNetworkFunctionRecord instantiate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Object scripts, Collection<VimInstance> vimInstances) throws Exception;
	/**
	 * This operation allows retrieving 
	 * VNF instance state and attributes.
	 */
	void query();
	
	/**
	 * This operation allows scaling 
	 * (out/in, up/down) a VNF instance.
	 * @param scaleOut
	 * @param virtualNetworkFunctionRecord
	 * @param component
	 * @param scripts
	 * @param dependency
	 */
	VirtualNetworkFunctionRecord scale(Action scaleOut, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance component, Object scripts, VNFRecordDependency dependency) throws Exception;
	
	/**
	 * This operation allows verifying if 
	 * the VNF instantiation is possible.
	 */
	void checkInstantiationFeasibility();
	
	/**
	 * This operation allows verifying if 
	 * the VNF instantiation is possible.
	 */
	VirtualNetworkFunctionRecord heal(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance component, String cause) throws Exception;
	
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
	
	VirtualNetworkFunctionRecord modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFRecordDependency dependency) throws Exception;
	
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
	VirtualNetworkFunctionRecord terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;
}
