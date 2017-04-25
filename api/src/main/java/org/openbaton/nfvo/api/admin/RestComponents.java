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

package org.openbaton.nfvo.api.admin;

import com.google.gson.JsonObject;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.Valid;
import org.openbaton.catalogue.nfvo.ServiceCredentials;
import org.openbaton.nfvo.security.interfaces.ComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  /** Enable a new Service. this generates a new AES Key that must be used in the Service SDK */
  @ApiOperation(
    value = "Enable Service",
    notes =
        "Enable a new Service. this generates a new AES Key that must be used in the Service SDK"
  )
  @RequestMapping(
    value = "/services",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public byte[] enableService(
      @RequestHeader(value = "project-id") String projectId,
      @RequestBody @Valid JsonObject serviceRegisterBody)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

    return componentManager.enableService(serviceRegisterBody, projectId);
  }

  @ApiOperation(value = "Register Service", notes = "Register a already enabled Service.")
  @RequestMapping(
    value = "/services",
    method = RequestMethod.POST,
    consumes = MediaType.TEXT_PLAIN_VALUE,
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public ServiceCredentials registerService(
      @RequestHeader(value = "project-id") String projectId,
      @RequestBody String serviceRegisterBody)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

    return componentManager.registerService(serviceRegisterBody);
  }

  //  /** Enable a new Manager. this generates a new Rabbit User that must be used in the Manager SDK */
  //  @ApiOperation(
  //    value = "Enable Manager",
  //    notes =
  //        "Enable a new Manager. this generates a Rabbit user that must be used in the Manager SDK"
  //  )
  //  @RequestMapping(
  //    value = "/managers",
  //    method = RequestMethod.POST,
  //    consumes = MediaType.APPLICATION_JSON_VALUE,
  //    produces = MediaType.APPLICATION_JSON_VALUE
  //  )
  //  @ResponseStatus(HttpStatus.CREATED)
  //  public ManagerCredentials enableManager(
  //      @RequestHeader(value = "project-id") String projectId,
  //      @RequestBody @Valid JsonObject serviceRegisterBody)
  //      throws IOException {
  //
  //    return componentManager.enableManager(serviceRegisterBody);
  //  }
}
