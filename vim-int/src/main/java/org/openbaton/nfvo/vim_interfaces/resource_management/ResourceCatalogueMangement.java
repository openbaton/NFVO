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

public interface ResourceCatalogueMangement {
	
	/**
	 * This operation allows retrieving the list of catalogued virtualised 
	 * resources, and/or a specific catalogued resource on which the 
	 * consumer is allowed to perform subsequent operations.
	 */
	void query();
	
	/**
	 * This operation provides change notifications on virtualised resources 
	 * catalogues managed by the producer functional block.
	 */
	void notifyChange();
}
