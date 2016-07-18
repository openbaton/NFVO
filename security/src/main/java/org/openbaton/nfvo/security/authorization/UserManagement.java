package org.openbaton.nfvo.security.authorization;

import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.Role.RoleEnum;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.PasswordWeakException;
import org.openbaton.nfvo.repositories.UserRepository;
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

/**
 * Created by lto on 25/02/16.
 */
@Service
@ConfigurationProperties
public class UserManagement implements org.openbaton.nfvo.security.interfaces.UserManagement {

  @Value("${nfvo.users.password.strength:true}")
  private boolean checkStrength;

  @Autowired private UserRepository userRepository;

  @Autowired
  @Qualifier("customUserDetailsService")
  private UserDetailsManager userDetailsManager;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUserName = authentication.getName();
    return queryDB(currentUserName);
  }

  @Override
  public User add(User user) throws PasswordWeakException {

    checkCurrentUserObAdmin(getCurrentUser());

    if (checkStrength && !isPasswordStrong(user.getPassword())) {
      throw new PasswordWeakException(
          "The chosen password is too weak. Password must be at least 8 chars and contain one lower case letter, one "
              + "upper case letter and one digit");
    }

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

  private boolean isPasswordStrong(String password) {
    return password.matches("(?=.*[A-Z]).*")
        && password.matches("(?=.*[a-z]).*")
        && password.matches("(?=.*[0-9]).*");
  }

  private void checkCurrentUserObAdmin(User currentUser) {
    if (currentUser.getRoles().iterator().next().getRole().ordinal()
        != RoleEnum.OB_ADMIN.ordinal()) {
      throw new UnauthorizedUserException("Sorry only OB_ADMIN can add/delete/update/query Users");
    }
  }

  @Override
  public void delete(User user) {
    checkCurrentUserObAdmin(getCurrentUser());
    userDetailsManager.deleteUser(user.getUsername());
    userRepository.delete(user);
  }

  @Override
  public User update(User new_user) {

    checkCurrentUserObAdmin(getCurrentUser());
    String[] roles = new String[new_user.getRoles().size()];

    Role[] objects = new_user.getRoles().toArray(new Role[0]);
    for (int i = 0; i < new_user.getRoles().size(); i++) {
      roles[i] = objects[i].getRole() + ":" + objects[i].getProject();
    }

    org.springframework.security.core.userdetails.User userToUpdate =
        new org.springframework.security.core.userdetails.User(
            new_user.getUsername(),
            BCrypt.hashpw(new_user.getPassword(), BCrypt.gensalt(12)),
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
    checkCurrentUserObAdmin(getCurrentUser());
    return userRepository.findAll();
  }

  @Override
  public User query(String username) {
    checkCurrentUserObAdmin(getCurrentUser());
    log.trace("Looking for user: " + username);
    return userRepository.findFirstByUsername(username);
  }

  @Override
  public User queryById(String id) {
    checkCurrentUserObAdmin(getCurrentUser());
    log.trace("Looking for user with id: " + id);
    return userRepository.findFirstById(id);
  }

  @Override
  public User queryDB(String username) {
    return userRepository.findFirstByUsername(username);
  }

  @Override
  public void changePassword(String oldPwd, String newPwd) throws UnauthorizedUserException {
    log.debug("Got old password: " + oldPwd);
    userDetailsManager.changePassword(oldPwd, newPwd);
  }
}
