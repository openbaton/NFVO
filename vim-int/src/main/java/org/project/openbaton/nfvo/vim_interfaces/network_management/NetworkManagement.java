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

import org.project.openbaton.catalogue.nfvo.Network;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.exceptions.VimException;

import java.util.List;


/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkManagement {
    
	/**
     * This operation allows adding new Network
     * to the network repository.
     * @param vimInstance
     * @param network
     */
    Network add(VimInstance vimInstance, Network network) throws VimException;

    /**
	 * This operation allows deleting in the Networks
     * from the network repository.
     * @param vimInstance
     * @param network
     */
    void delete(VimInstance vimInstance, Network network) throws VimException;
    
    /**
	 * This operation allows updating the Network
     * in the network repository.
     * @param vimInstance
     * @param updatingNetwork
     */
    Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException;
    
    /**
	 * This operation allows querying the information of 
	 * the Networks in the network repository.
     * @param vimInstance
     */
    List<Network> queryNetwork(VimInstance vimInstance) throws VimException;
    
    /**
     * This operation allows querying the information of 
     * the Networks in the network repository.
     * @param vimInstance
     */
    Network query(VimInstance vimInstance, String extId) throws VimException;
}
