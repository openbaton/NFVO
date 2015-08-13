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

package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.common.exceptions.VimException;

import java.util.List;

/**
 * Created by lto on 13/05/15.
 */
public interface VimManagement {

    /**
     * This operation allows adding a datacenter
     * into the datacenter repository.
     * @param vimInstance
     */
    VimInstance add(VimInstance vimInstance) throws VimException;

    /**
     * This operation allows deleting the datacenter
     * from the datacenter repository.
     * @param id
     */
    void delete(String id);

    /**
     * This operation allows updating the datacenter
     * in the datacenter repository.
     * @param new_vimInstance
     * @param id
     */
    VimInstance update(VimInstance new_vimInstance, String id) throws VimException;

    /**
     * This operation allows querying the information of
     * the datacenters in the datacenter repository.
     */
    List<VimInstance> query();

    /**
     * This operation allows querying the information of
     * the datacenter in the datacenter repository.
     */
    VimInstance query(String id);

    void refresh(VimInstance vimInstance) throws VimException;
}
