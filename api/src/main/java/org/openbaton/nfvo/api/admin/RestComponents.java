/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.openbaton.catalogue.api.ServiceCreateBody;
import org.openbaton.catalogue.security.ServiceMetadata;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.ComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/components")
public class RestComponents {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ComponentManager componentManager;
  @Autowired private Gson gson;

  /**
   * Create a new Service. This generates a new AES Key that can be used for registering the
   * Service.
   *
   * @param projectId
   * @param serviceCreateBody
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  @ApiOperation(
    value = "Create Service",
    notes =
        "Enable a new Service. This generates a new AES Key that must be used in the Service SDK"
  )
  @RequestMapping(
    value = "/services/create",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
  public String createService(
      @RequestHeader(value = "project-id") String projectId,
      @RequestBody @Valid ServiceCreateBody serviceCreateBody)
      throws NotFoundException, MissingParameterException {
    String serviceName = serviceCreateBody.getName();
    List<String> projects = serviceCreateBody.getRoles();
    return componentManager.createService(serviceName, projectId, projects);
  }

  /**
   * Registers a Service. For this to work, the Service has to be already created. The request body
   * is expected to be a String. This String has to be the word 'register' encrypted with the Key
   * obtained while creating the Service. This method returns a token encapsulated in a Json object
   * which can be used by the Service to issue requests to the NFVO API.
   *
   * @param serviceRegisterBody
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  @ApiOperation(value = "Register Service", notes = "Register an already created Service.")
  @RequestMapping(
    value = "/services/register",
    method = RequestMethod.POST,
    consumes = MediaType.TEXT_PLAIN_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public JsonObject registerService(@RequestBody String serviceRegisterBody)
      throws NoSuchAlgorithmException, NotFoundException, InvalidKeyException, BadPaddingException,
          NoSuchPaddingException, IllegalBlockSizeException {

    JsonObject response = new JsonObject();
    response.add("token", new JsonPrimitive(componentManager.registerService(serviceRegisterBody)));
    return response;
  }

  /** Delete a new Service. */
  @ApiOperation(value = "Delete Service", notes = "Remove a specific service")
  @RequestMapping(
    value = "/services/{id}",
    method = RequestMethod.DELETE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
  public void deleteService(
      @RequestHeader(value = "project-id") String projectId, @PathVariable("id") String id) {
    log.debug("id is " + id);
    componentManager.removeService(id);
  }

  /** Delete multiple Services. */
  @ApiOperation(
    value = "Delete multiple Services",
    notes = "In the Request Body pass a list of service ids that have to be deleted"
  )
  @RequestMapping(
    value = "/services/multipledelete",
    method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
  public void multipleDelete(@RequestBody @Valid List<String> ids) {
    if (componentManager != null) {
      for (String id : ids) {
        log.info("Removing Service with id: " + id);
        componentManager.removeService(id);
      }
    }
  }

  /** List all Services. */
  @ApiOperation(value = "List Services", notes = "List all services")
  @RequestMapping(
    value = "/services",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
  public List<ServiceMetadata> listServices(@RequestHeader(value = "project-id") String projectId) {

    return (List<ServiceMetadata>) componentManager.listServices();
  }
}
