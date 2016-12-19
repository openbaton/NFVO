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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;
import javax.validation.Valid;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PasswordWeakException;
import org.openbaton.nfvo.security.interfaces.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class RestUsers {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private UserManagement userManagement;
  @Autowired private Gson gson;

  /**
   * Adds a new User to the Users repository
   *
   * @param user
   * @return user
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public User create(@RequestBody @Valid User user)
      throws PasswordWeakException, NotAllowedException, BadRequestException, NotFoundException {
    log.info("Adding user: " + user.getUsername());
    if (isAdmin()) {
      user = userManagement.add(user);
      //      user.setPassword(null);
    } else {
      throw new NotAllowedException("Forbidden to create a new user");
    }
    return user;
  }

  /**
   * Removes the User from the Users repository
   *
   * @param id : the id of user to be removed
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") String id)
      throws NotAllowedException, NotFoundException, BadRequestException {
    log.info("Removing user with id " + id);
    if (isAdmin()) {
      if (!userManagement.getCurrentUser().getId().equals(id)) {
        User user = userManagement.query(id);
        userManagement.delete(user);
      } else {
        throw new NotAllowedException("You can't delete yourself. Please ask another admin.");
      }
    } else {
      throw new NotAllowedException("Forbidden to delete a user");
    }
  }

  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(@RequestBody @Valid List<String> ids)
      throws NotAllowedException, NotFoundException {
    if (userManagement != null) {
      for (String id : ids) {
        log.info("removing User with id " + id);
        userManagement.delete(userManagement.query(id));
      }
    }
  }

  /**
   * Returns the list of the Users available
   *
   * @return List<User>: The list of Users available
   */
  @RequestMapping(method = RequestMethod.GET)
  public Iterable<User> findAll() {
    log.trace("Find all Users");
    return userManagement.query();
  }

  /**
   * Returns the User selected by username
   *
   * @param username : The username of the User
   * @return User: The User selected
   */
  @RequestMapping(value = "{username}", method = RequestMethod.GET)
  public User findById(@PathVariable("username") String username) throws NotFoundException {
    log.trace("find User with username " + username);
    User user = userManagement.query(username);
    log.trace("Found User: " + user);
    return user;
  }

  @RequestMapping(value = "current", method = RequestMethod.GET)
  public User findCurrentUser() throws NotFoundException {
    User user = userManagement.getCurrentUser();
    log.trace("Found User: " + user);
    return user;
  }

  /**
   * Updates the User
   *
   * @param new_user : The User to be updated
   * @return User The User updated
   */
  @RequestMapping(
    value = "{username}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public User update(@RequestBody @Valid User new_user)
      throws NotAllowedException, BadRequestException, NotFoundException {
    return userManagement.update(new_user);
  }

  @RequestMapping(
    value = "changepwd",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void changePassword(@RequestBody /*@Valid*/ JsonObject newPwd)
      throws UnauthorizedUserException, PasswordWeakException {
    log.debug("Changing password");
    JsonObject jsonObject = gson.fromJson(newPwd, JsonObject.class);
    userManagement.changePassword(
        jsonObject.get("old_pwd").getAsString(), jsonObject.get("new_pwd").getAsString());
  }

  @RequestMapping(
    value = "changepwd/{username}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void changePasswordOf(
      @PathVariable("username") String username, @RequestBody /*@Valid*/ JsonObject newPwd)
      throws UnauthorizedUserException, PasswordWeakException, NotFoundException,
          NotAllowedException {
    log.debug("Changing password of user " + username);
    if (isAdmin()) {
      JsonObject jsonObject = gson.fromJson(newPwd, JsonObject.class);
      userManagement.changePasswordOf(username, jsonObject.get("new_pwd").getAsString());
    } else {
      throw new NotAllowedException(
          "Forbidden to change password of other users. Only admins can do this.");
    }
  }

  public boolean isAdmin() throws NotAllowedException, NotFoundException {
    User currentUser = userManagement.getCurrentUser();
    log.trace("Check user if admin: " + currentUser.getUsername());
    for (Role role : currentUser.getRoles()) {
      if (role.getRole().ordinal() == Role.RoleEnum.ADMIN.ordinal()) {
        return true;
      }
    }
    return false;
  }
}
