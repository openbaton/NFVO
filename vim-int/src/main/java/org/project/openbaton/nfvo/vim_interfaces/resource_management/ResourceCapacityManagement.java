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

package org.project.openbaton.nfvo.vim_interfaces.resource_management;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourceCapacityManagement {
	
	/**
	 * This operation allows querying the capacity usage of an NFVI-PoP. The operation 
	 * can be used to gather information at different levels, from specific virtualised 
	 * partition capacity usage, to total capacity availability in the NFVI-PoP.
	 */
	String query(String pop);
	
	/**
	 * This operation allows notifying about capacity changes in the NFVI-PoP.
	 */
	void notifyChanges(String notification);
	
}
