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
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PasswordWeakException;
import org.openbaton.nfvo.core.interfaces.UserManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @ApiOperation(
    value = "Adding a User",
    notes = "The User data is passed as JSON in the Request Body"
  )
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
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
  @ApiOperation(
    value = "Remove a User",
    notes = "Removes the user with the id specified in the URL. Admin privileges needed!"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
  public void delete(@PathVariable("id") String id) throws NotAllowedException, NotFoundException {
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

  @ApiOperation(
    value = "Remove multiple Users",
    notes = "Removes all users part of the List of ids passed in the Request Body"
  )
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(@RequestBody @Valid List<String> ids) throws NotFoundException {
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
  @ApiOperation(value = "Retrieve all Users", notes = "Returns all registered users")
  @RequestMapping(method = RequestMethod.GET)
  public List<User> findAll() {
    log.trace("Find all Users");
    return (List<User>) userManagement.query();
  }

  /**
   * Returns the User selected by username
   *
   * @param username : The username of the User
   * @return User: The User selected
   */
  @ApiOperation(
    value = "Retrieve a User",
    notes = "Retrieves a user based on the username specified in the URL"
  )
  @RequestMapping(value = "{username}", method = RequestMethod.GET)
  public User findById(@PathVariable("username") String username) throws NotFoundException {
    log.trace("find User with username " + username);
    User user = userManagement.query(username);
    log.trace("Found User: " + user);
    return user;
  }

  @ApiOperation(value = "Retrieve the current User", notes = "Returns the user currently accessing")
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
  @ApiOperation(
    value = "Update a User",
    notes =
        "Updates a user based on the username specified in the url and the updated user body in the request"
  )
  @RequestMapping(
    value = "{username}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
  public User update(@RequestBody @Valid User new_user)
      throws NotAllowedException, BadRequestException, NotFoundException {
    return userManagement.update(new_user);
  }

  @ApiOperation(
    value = "Changing the current User's password",
    notes = "The current user can change his password by providing a new one"
  )
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

  @ApiOperation(
    value = "Changing a User's password",
    notes = "If you want to change another User's password, you have to be an admin"
  )
  @RequestMapping(
    value = "changepwd/{username}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
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

  public boolean isAdmin() throws NotFoundException {
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
