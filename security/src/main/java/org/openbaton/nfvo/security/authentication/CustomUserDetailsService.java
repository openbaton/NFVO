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

package org.openbaton.nfvo.security.authentication;

import java.util.*;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.Role.RoleEnum;
import org.openbaton.catalogue.security.User;
import org.openbaton.nfvo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsManager {

  @Autowired private UserRepository userRepository;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.security.admin.password:openbaton}")
  private String adminPwd;

  @Value("${nfvo.security.guest.password:guest}")
  private String guestPwd;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findFirstByUsername(username);
    if (user == null) throw new UsernameNotFoundException(username);
    return user;
  }

  @PostConstruct
  public void init() {
    log.debug("Creating initial Users...");

    User admin = userRepository.findFirstByUsername("admin");
    if (!userExists("admin")) {
      User ob_admin = new User();
      ob_admin.setUsername("admin");
      ob_admin.setEnabled(true);
      ob_admin.setPassword(BCrypt.hashpw(adminPwd, BCrypt.gensalt(12)));
      Set<Role> roles = new HashSet<>();
      Role role = new Role();
      role.setRole(RoleEnum.ADMIN);
      role.setProject("*");
      roles.add(role);
      ob_admin.setRoles(roles);
      createUser(ob_admin);
    } else {
      log.debug("Admin user exists already.");
    }

    log.debug("Users in the DB: ");
    for (User user : userRepository.findAll()) {
      log.debug("" + user);
    }
  }

  @Override
  public void createUser(UserDetails user) {
    if (userExists(user.getUsername())) {
      log.warn("User " + user.getUsername() + " already exists.");
      return;
    }
    userRepository.save((User) user);
    log.debug("Created user " + user.getUsername());
  }

  @Override
  public void updateUser(UserDetails user) {
    if (userExists(user.getUsername())) {
      userRepository.save((User) user);
      log.debug("Updated user " + user.getUsername());
      return;
    }
    log.warn(
        "User " + user.getUsername() + " does not exist, so no update operation was executed.");
  }

  @Override
  public void deleteUser(String username) {
    User user = userRepository.findFirstByUsername(username);
    if (user == null) {
      log.warn("User " + username + " does not exist and therefore cannot be deleted.");
      return;
    }
    userRepository.delete(userRepository.findFirstByUsername(username).getId());
    log.debug("Successfully deleted user " + username);
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUserName = authentication.getName();
    log.debug("Changing password of user: " + currentUserName);
    User user = userRepository.findFirstByUsername(currentUserName);
    if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
      throw new UnauthorizedUserException("Old password is wrong.");
    }
    if (!(authentication instanceof AnonymousAuthenticationToken)) { // TODO is this line needed?
      user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
      userRepository.save(user);
      log.debug("Password of user " + currentUserName + " has been changed successfully.");
    }
  }

  @Override
  public boolean userExists(String username) {
    return (userRepository.findFirstByUsername(username) != null);
  }
}
