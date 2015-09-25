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

package org.project.openbaton.nfvo.api;

import org.project.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.project.openbaton.nfvo.core.interfaces.VirtualLinkManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1//virtual-link-descriptors")
public class RestVirtualLink {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VirtualLinkManagement virtualLinkManagement;

    /**
     * Adds a new Configuration to the Configurations repository
     *
     * @param virtualLinkDescriptor
     * @return virtualLinkDescriptor
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VirtualLinkDescriptor create(@RequestBody @Valid VirtualLinkDescriptor virtualLinkDescriptor) {
        log.trace("Adding VirtualLinkDescriptor: " + virtualLinkDescriptor);
        log.debug("Adding VirtualLinkDescriptor");
        return virtualLinkManagement.add(virtualLinkDescriptor);
    }

    /**
     * Removes the Configuration from the Configurations repository
     *
     * @param id : the id of virtualLinkDescriptor to be removed
     */

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        log.debug("removing VirtualLink with id " + id);
        virtualLinkManagement.delete(id);
    }

    /**
     * Returns the list of the Configurations available
     *
     * @return List<Configuration>: The list of Configurations available
     */
    @RequestMapping(method = RequestMethod.GET)
    public Iterable<VirtualLinkDescriptor> findAllDescriptors() {
        log.debug("Find all Configurations");
        return virtualLinkManagement.queryDescriptors();
    }

    /**
     * Returns the Configuration selected by id
     *
     * @param id : The id of the Configuration
     * @return Configuration: The Configuration selected
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public VirtualLinkDescriptor findDescriptorById(@PathVariable("id") String id) {
        log.debug("find Configuration with id " + id);
        VirtualLinkDescriptor virtualLinkDescriptor = virtualLinkManagement.queryDescriptor(id);
        log.trace("Found Configuration: " + virtualLinkDescriptor);
        return virtualLinkDescriptor;
    }

    /**
     * Updates the Configuration
     *
     * @param virtualLinkDescriptor_new : The Configuration to be updated
     * @param id                        : The id of the Configuration
     * @return Configuration The Configuration updated
     */

    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VirtualLinkDescriptor update(
            @RequestBody @Valid VirtualLinkDescriptor virtualLinkDescriptor_new,
            @PathVariable("id") String id) {
        log.trace("updating VirtualLinkDescriptor with id " + id + " with values: "
                + virtualLinkDescriptor_new);
        log.debug("updating VirtualLinkDescriptor with id " + id);
        return virtualLinkManagement.update(virtualLinkDescriptor_new, id);
    }
}
