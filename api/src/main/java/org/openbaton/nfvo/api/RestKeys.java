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
import com.google.gson.JsonObject;
import org.openbaton.catalogue.security.Key;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PasswordWeakException;
import org.openbaton.nfvo.core.interfaces.KeyManagement;
import org.openbaton.nfvo.security.interfaces.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/keys")
public class RestKeys {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private KeyManagement keyManagement;
  @Autowired private Gson gson;

  /**
   * Adds a new Key
   *
   * @param key
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void importKey(
      @RequestHeader(value = "project-id") String projectId, @RequestBody @Valid Key key) {
    keyManagement.addKey(projectId, key.getName(), key.getPublicKey());
  }

  /**
   * Generate a new Key with the given name for the given project
   *
   * @param name : name of the key to be created
   */
  @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public byte[] generateKey(
      @RequestHeader(value = "project-id") String projectId, @RequestBody String name)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException,
          IOException {
    return keyManagement.generateKey(projectId, name);
  }

  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestHeader(value = "project-id") String projectId, @RequestBody @Valid List<String> ids)
      throws NotFoundException {
    for (String id : ids) {
      keyManagement.delete(projectId, id);
    }
  }

  /**
   * Returns the list of the Users available
   *
   * @return List<User>: The list of Users available
   */
  @RequestMapping(method = RequestMethod.GET)
  public Iterable<Key> findAll(@RequestHeader(value = "project-id") String projectId) {
    return keyManagement.query(projectId);
  }

  /**
   * Returns the Key selected by id
   *
   * @param id : The id of the Key
   * @return User: The Key selected
   */
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public Key findById(
      @RequestHeader(value = "project-id") String projectId, @PathVariable("id") String id)
      throws NotFoundException {
    return keyManagement.queryById(projectId, id);
  }
}
