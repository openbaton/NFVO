/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.api.utils.Utils;
import org.openbaton.nfvo.core.interfaces.ComponentManager;
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

@RestController
@RequestMapping("/api/v1/events")
public class RestEvent {

  //	TODO add log prints
  //	private Logger log = LoggerFactory.getLogger(this.getClass());

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Autowired private EventDispatcher eventDispatcher;
  @Autowired private ComponentManager componentManager;
  @Autowired private EventManagement eventManagement;
  @Autowired private Utils utils;

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
      @RequestHeader(value = "project-id") String projectId,
      @RequestHeader(value = "authorization") String token)
      throws MissingParameterException, IllegalBlockSizeException, NoSuchPaddingException,
          BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadFormatException,
          NotFoundException, BadRequestException {
    String[] tokenArray = token.split(" ");
    if (tokenArray.length < 2) throw new BadFormatException("The passed token has a wrong format.");
    token = tokenArray[1];
    if (endpoint.getProjectId() == null || endpoint.getProjectId().equals(""))
      endpoint.setProjectId(projectId);
    if ((!componentManager.isService(token) || !utils.isAdmin())
        && endpoint.getProjectId().equals("*"))
      throw new BadRequestException("Only services and admin can register to all events");
    if (!utils.isAdmin()
        && !componentManager.isService(token)
        && utils
            .getCurrentUser()
            .getRoles()
            .stream()
            .noneMatch(r -> r.getProject().equals(endpoint.getProjectId())))
      throw new BadRequestException("Only services and admin can register to all events");
    return eventDispatcher.register(gson.toJson(endpoint));
  }

  /**
   * Removes the EventEndpoint from the EventEndpoint repository
   *
   * @param id : The Event's id to be deleted
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unregister(@PathVariable("id") String id) throws NotFoundException {
    eventDispatcher.unregister(id);
  }

  /**
   * Removes multiple EventEndpoint from the EventEndpoint repository
   *
   * @param ids: The List of the EventEndpoint Id to be deleted
   * @throws NotFoundException if the requested event endpoint does not exist
   */
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(@RequestBody @Valid List<String> ids) throws NotFoundException {
    for (String id : ids) eventDispatcher.unregister(id);
  }

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public List<EventEndpoint> getEventEndpoints(
      @RequestHeader(value = "project-id") String projectId) {
    List<EventEndpoint> eventEndpoints = new ArrayList<>();
    eventManagement.queryByProjectId(projectId).forEach(eventEndpoints::add);
    eventManagement.queryByProjectId("*").forEach(eventEndpoints::add);
    return eventEndpoints;
  }

  @RequestMapping(
    value = "/{id}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public EventEndpoint getEventEndpoint(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
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
