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

package org.openbaton.nfvo.vim_interfaces.resource_management;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourceFaultManagement {

	/**
	 * This operation allows collecting 
	 * virtualised resource fault information.
	 */
	void getInformation();
	
	/**
	 * This operation allows providing fault 
	 * notifications on virtualised resources.
	 */
	void notifyInformation();
}
