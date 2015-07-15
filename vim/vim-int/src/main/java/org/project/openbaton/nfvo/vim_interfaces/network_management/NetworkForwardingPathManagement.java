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

package org.project.openbaton.nfvo.vim_interfaces.network_management;

import org.project.openbaton.catalogue.mano.descriptor.NetworkForwardingPath;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkForwardingPathManagement {
	
	/**
	 * This operation allows creating a Network Forwarding Path.
	 */
	NetworkForwardingPath create();

	/**
	 * This operation allows updating the information 
	 * associated with a Network Forwarding Path.
	 */
	NetworkForwardingPath update();

	/**
	 * This operation allows deleting a 
	 * Network Forwarding Path.
	 */
	void delete();

	/**
	 * This operation allows querying information about 
	 * a specified Network Forwarding Path instance.
	 */
	NetworkForwardingPath query();

	/**
	 * This operation allows providing information 
	 * about a Network Forwarding Path rule.
	 */
	void notifyInformation();
}
