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

package org.openbaton.nfvo.security.authentication;

import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.Role.RoleEnum;
import org.openbaton.catalogue.security.User;
import org.openbaton.nfvo.repositories.UserRepository;
import org.openbaton.nfvo.security.interfaces.ProjectManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

@Component
public class CustomUserDetailsService implements CommandLineRunner, UserDetailsManager {

  @Autowired private UserRepository userRepository;

  @Autowired
  @Qualifier("inMemManager")
  private UserDetailsManager inMemManager;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.security.admin.password:openbaton}")
  private String adminPwd;

  @Value("${nfvo.security.guest.password:guest}")
  private String guestPwd;

  @Autowired private ProjectManagement projectManagement;

  @Value("${nfvo.security.project.name:default}")
  private String projectDefaultName;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return inMemManager.loadUserByUsername(username);
  }

  @Override
  public void run(String... args) throws Exception {

    log.debug("Creating initial Users...");

    User admin = userRepository.findFirstByUsername("admin");
    if (admin == null) {
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
      admin = userRepository.save(ob_admin);
    }
    if (!inMemManager.userExists("admin")) {
      UserDetails adminInMem =
          new org.springframework.security.core.userdetails.User(
              admin.getUsername(),
              admin.getPassword(),
              admin.isEnabled(),
              true,
              true,
              true,
              AuthorityUtils.createAuthorityList("ADMIN:*"));
      inMemManager.createUser(adminInMem);
    } else {
      log.debug("Admin" + inMemManager.loadUserByUsername("admin"));
    }

    log.debug("User in the DB: ");
    for (User user : userRepository.findAll()) {
      log.debug("" + user);
    }

    for (User user : userRepository.findAll()) {
      if (!user.getUsername().equals("admin") && !user.getUsername().equals("guest")) {
        String[] roles = new String[user.getRoles().size()];
        for (int i = 0; i < user.getRoles().size(); i++) {
          roles[i] =
              user.getRoles().toArray(new Role[0])[i].getRole()
                  + ":"
                  + user.getRoles().toArray(new Role[0])[i].getProject();
        }
        UserDetails userDetails =
            new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                AuthorityUtils.createAuthorityList(roles));
        inMemManager.createUser(userDetails);
      }
    }

    log.debug("Users in UserDetailManager: ");
    log.debug("ADMIN: " + inMemManager.loadUserByUsername("admin"));

    log.debug("Creating initial Project...");

    if (!projectManagement.query().iterator().hasNext()) {
      Project project = new Project();
      project.setName(projectDefaultName);
      project.setDescription("default project");

      projectManagement.add(project);
      log.debug("Created project: " + project);
    } else log.debug("One project is already existing");
  }

  @Override
  public void createUser(UserDetails user) {
    this.inMemManager.createUser(user);
  }

  @Override
  public void updateUser(UserDetails user) {
    inMemManager.updateUser(user);
    User userToUpdate = userRepository.findFirstByUsername(user.getUsername());
    userToUpdate.setPassword(user.getPassword());
    for (GrantedAuthority authority : user.getAuthorities()) {
      StringTokenizer stringTokenizer = new StringTokenizer(authority.getAuthority(), ":");
      String rl = stringTokenizer.nextToken();
      String pj = stringTokenizer.nextToken();
      boolean found = false;
      for (Role role : userToUpdate.getRoles()) {
        if (role.getProject().equals(pj)) {
          role.setRole(RoleEnum.valueOf(rl));
          found = true;
        }
      }
      if (!found) {
        Role role = new Role();
        role.setRole(RoleEnum.valueOf(rl));
        role.setProject(pj);
        userToUpdate.getRoles().add(role);
      }
    }
    userRepository.save(userToUpdate);
  }

  @Override
  public void deleteUser(String username) {
    inMemManager.deleteUser(username);
    userRepository.delete(userRepository.findFirstByUsername(username).getId());
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUserName = authentication.getName();
    log.debug("Changing password of user: " + currentUserName);
    User user = userRepository.findFirstByUsername(currentUserName);
    if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
      throw new UnauthorizedUserException("Old password is wrong");
    }
    log.debug("changing pwd");
    inMemManager.changePassword(oldPassword, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
      userRepository.save(user);
    }
  }

  @Override
  public boolean userExists(String username) {
    return inMemManager.userExists(username)
        && (userRepository.findFirstByUsername(username) != null);
  }
}
