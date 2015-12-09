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

package org.openbaton.nfvo.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class RestEvent {

    //	TODO add log prints
//	private Logger log = LoggerFactory.getLogger(this.getClass());

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private EventDispatcher eventDispatcher;

    /**
     * Adds a new EventEndpoint to the EventEndpoint repository
     *
     * @param endpoint : Image to add
     * @return image: The image filled with values from the core
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EventEndpoint register(@RequestBody @Valid EventEndpoint endpoint) {

        return eventDispatcher.register(gson.toJson(endpoint));
    }

    /**
     * Removes the EventEndpoint from the EventEndpoint repository
     *
     * @param name : The Event's id to be deleted
     */
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable("id") String id) throws NotFoundException {
        eventDispatcher.unregister(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<EventEndpoint> getEvents() {

        List<EventEndpoint> events = new ArrayList<>();
        for (Action action : Action.values()) {
            EventEndpoint endpoint = new EventEndpoint();
            endpoint.setEvent(action);
            events.add(endpoint);
        }
        return events;
    }

}
