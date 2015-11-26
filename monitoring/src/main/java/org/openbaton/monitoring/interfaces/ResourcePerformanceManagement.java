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

package org.openbaton.monitoring.interfaces;

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.exceptions.MonitoringException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

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
	List<Item> getMeasurementResults(List<String> hostnames, List<String> metrics, String period) throws RemoteException, MonitoringException;

	/**
	 * This operation allows providing notifications with performance
	 * measurement results on virtualised resources.
	 */
	void notifyResults() throws RemoteException, MonitoringException;
}
