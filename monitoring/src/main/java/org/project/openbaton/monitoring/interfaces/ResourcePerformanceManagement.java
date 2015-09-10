/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.monitoring.interfaces;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.nfvo.Item;

import java.rmi.Remote;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourcePerformanceManagement extends Remote {
	
	/**
	 * This version must match the version of the plugin...
	 */
	String interfaceVersion = "1.0";
	
	/**
	 * This operation allows collecting performance measurement results 
	 * generated on virtualised resources.
	 */
	Item getMeasurementResults(VNFCInstance vnfcInstance, String metric, String period);

	/**
	 * This operation allows providing notifications with performance
	 * measurement results on virtualised resources.
	 */
	void notifyResults();

	String getType();
}
