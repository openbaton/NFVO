/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.security.authorization;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PasswordWeakException;
import org.openbaton.nfvo.repositories.UserRepository;
import org.openbaton.nfvo.security.authorization.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

/** Created by lto on 25/02/16. */
@Service
@ConfigurationProperties
public class UserManagement implements org.openbaton.nfvo.security.interfaces.UserManagement {

  @Value("${nfvo.users.password.strength:true}")
  private boolean checkStrength;

  @Autowired private UserRepository userRepository;

  @Autowired private org.openbaton.nfvo.security.interfaces.ProjectManagement projectManagement;

  @Autowired
  @Qualifier("customUserDetailsService")
  private UserDetailsManager userDetailsManager;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public User add(User user)
      throws PasswordWeakException, NotAllowedException, BadRequestException, NotFoundException {
    log.debug("Adding new user: " + user);

    if (userRepository.findFirstByUsername(user.getUsername()) != null) {
      throw new BadRequestException("Username exists already");
    }

    checkIntegrity(user);

    if (checkStrength) Utils.checkPasswordIntegrity(user.getPassword());

    String[] roles = new String[user.getRoles().size()];

    Role[] objects = user.getRoles().toArray(new Role[0]);
    for (int i = 0; i < user.getRoles().size(); i++) {
      roles[i] = objects[i].getRole() + ":" + objects[i].getProject();
    }

    org.springframework.security.core.userdetails.User userToAdd =
        new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)),
            user.isEnabled(),
            true,
            true,
            true,
            AuthorityUtils.createAuthorityList(roles));
    user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
    userDetailsManager.createUser(userToAdd);
    return userRepository.save(user);
  }

  @Override
  public void delete(User user) throws NotAllowedException {
    log.debug("Deleting user: " + user);
    userDetailsManager.deleteUser(user.getUsername());
    userRepository.delete(user);
  }

  @Override
  public User update(User new_user)
      throws NotAllowedException, BadRequestException, NotFoundException {

    User user = query(new_user.getId());
    if (!user.getUsername().equals(new_user.getUsername())) {
      throw new NotAllowedException("Forbidden to change the username");
    }
    new_user.setPassword(user.getPassword());

    checkIntegrity(new_user);

    String[] roles = new String[new_user.getRoles().size()];

    Role[] objects = new_user.getRoles().toArray(new Role[0]);
    for (int i = 0; i < new_user.getRoles().size(); i++) {
      roles[i] = objects[i].getRole() + ":" + objects[i].getProject();
    }

    org.springframework.security.core.userdetails.User userToUpdate =
        new org.springframework.security.core.userdetails.User(
            new_user.getUsername(),
            new_user.getPassword(),
            new_user.isEnabled(),
            true,
            true,
            true,
            AuthorityUtils.createAuthorityList(roles));
    userDetailsManager.updateUser(userToUpdate);
    return userRepository.save(new_user);
  }

  @Override
  public Iterable<User> query() {
    log.trace("Listing users");
    return userRepository.findAll();
  }

  @Override
  public User query(String id) throws NotFoundException {
    log.trace("Looking for user with id: " + id);
    User user = userRepository.findFirstById(id);
    if (user == null) {
      throw new NotFoundException("Not found user with id: " + id);
    }
    return user;
  }

  @Override
  public User queryByName(String username) throws NotFoundException {
    log.trace("Get user: " + username);
    User user = userRepository.findFirstByUsername(username);
    if (user == null) {
      throw new NotFoundException("Not found user " + username);
    }
    return user;
  }

  @Override
  public void changePassword(String oldPwd, String newPwd)
      throws UnauthorizedUserException, PasswordWeakException {
    log.debug("Got old password: " + oldPwd);
    if (checkStrength) Utils.checkPasswordIntegrity(newPwd);
    userDetailsManager.changePassword(oldPwd, newPwd);
  }

  @Override
  public User changePasswordOf(String userName, String newPwd)
      throws PasswordWeakException, NotFoundException {
    User user = userRepository.findFirstByUsername(userName);
    if (user == null) throw new NotFoundException("Not found user " + userName);
    if (checkStrength) Utils.checkPasswordIntegrity(newPwd);
    user.setPassword(BCrypt.hashpw(newPwd, BCrypt.gensalt(12)));

    String[] roles = new String[user.getRoles().size()];

    Role[] objects = user.getRoles().toArray(new Role[0]);
    for (int i = 0; i < user.getRoles().size(); i++) {
      roles[i] = objects[i].getRole() + ":" + objects[i].getProject();
    }
    org.springframework.security.core.userdetails.User userToUpdate =
        new org.springframework.security.core.userdetails.User(
            userName,
            user.getPassword(),
            user.isEnabled(),
            true,
            true,
            true,
            AuthorityUtils.createAuthorityList(roles));
    userDetailsManager.updateUser(userToUpdate);
    return userRepository.save(user);
  }

  public void checkIntegrity(User user)
      throws BadRequestException, NotFoundException, NotAllowedException {
    if (user.getUsername() == null || user.getUsername().equals("")) {
      throw new BadRequestException("Username must be provided");
    }
    if (user.getPassword() == null || user.getPassword().equals("")) {
      throw new BadRequestException("Password must be provided");
    }
    if (user.getEmail() != null && !user.getEmail().equals("")) {
      String EMAIL_PATTERN =
          "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
      Pattern pattern = Pattern.compile(EMAIL_PATTERN);
      if (!pattern.matcher(user.getEmail()).matches()) {
        throw new BadRequestException("Email is not well formatted");
      }
    }
    Set<String> assignedProjects = new HashSet<>();
    if (user.getRoles().isEmpty()) {
      throw new BadRequestException("At least one role must be provided");
    }
    for (Role role : user.getRoles()) {
      if (role.getProject() == null || role.getProject().equals("")) {
        throw new BadRequestException("Project must be provided");
      }
      if (role.getRole() == null || role.getRole().equals("")) {
        throw new BadRequestException("Role must be provided");
      }
      if (!role.getProject().equals("*")) {
        Project project = projectManagement.queryByName(role.getProject());
        if (project == null) {
          throw new BadRequestException("Not found project " + role.getProject());
        }
        if (!assignedProjects.contains(role.getProject())) {
          assignedProjects.add(role.getProject());
        } else {
          throw new BadRequestException("Only one role per project");
        }
      }
    }
  }

  @Override
  public User getCurrentUser() throws NotFoundException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }
    String currentUserName = authentication.getName();
    return this.queryByName(currentUserName);
  }
}
