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
import org.openbaton.nfvo.core.interfaces.EventManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/events")
public class RestEvent {

  //	TODO add log prints
  //	private Logger log = LoggerFactory.getLogger(this.getClass());

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Autowired private EventDispatcher eventDispatcher;

  @Autowired private EventManagement eventManagement;

  /**
   * Adds a new EventEndpoint to the EventEndpoint repository
   *
   * @param endpoint : Image to add
   * @return image: The image filled with values from the core
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public EventEndpoint register(
      @RequestBody @Valid EventEndpoint endpoint,
      @RequestHeader(value = "project-id") String projectId) {
    endpoint.setProjectId(projectId);
    return eventDispatcher.register(gson.toJson(endpoint));
  }

  /**
   * Removes the EventEndpoint from the EventEndpoint repository
   *
   * @param id : The Event's id to be deleted
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unregister(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    eventDispatcher.unregister(id, projectId);
  }

  /**
   * Removes multiple EventEndpoint from the EventEndpoint repository
   *
   * @param ids: The List of the EventEndpoint Id to be deleted
   * @throws NotFoundException
   */
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    for (String id : ids) eventDispatcher.unregister(id, projectId);
  }

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<EventEndpoint> getEventEndpoints(
      @RequestHeader(value = "project-id") String projectId) {
    return eventManagement.queryByProjectId(projectId);
  }

  @RequestMapping(
    value = "/{id}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public EventEndpoint getEventEndpoint(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    return eventManagement.query(id, projectId);
  }

  @RequestMapping(
    value = "/actions",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Action[] getAvailableEvents() {
    return Action.values();
  }
}
