package org.openbaton.nfvo.security.authorization;

import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
import org.openbaton.nfvo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 25/02/16.
 */
@Service
public class UserManagement implements org.openbaton.nfvo.security.interfaces.UserManagement {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsManager userDetailsManager;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public User add(User user) {
        String[] roles = new String[user.getRoles().size()];

        Role[] objects = user.getRoles().toArray(new Role[0]);
        for (int i = 0; i < user.getRoles().size(); i++) {
            roles[i] = objects[i].getRole() + ":" + objects[i].getProject();
        }

        org.springframework.security.core.userdetails.User userToAdd= new org.springframework.security.core.userdetails.User(user.getUsername(), BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)), user.isEnabled(), true, true, true, AuthorityUtils.createAuthorityList(roles));
        userDetailsManager.createUser(userToAdd);
        return userRepository.save(user);
    }

    @Override
    public void delete(User user) {
        userDetailsManager.deleteUser(user.getUsername());
        userRepository.delete(user);
    }

    @Override
    public User update(User new_user) {

        String[] roles = new String[new_user.getRoles().size()];

        Role[] objects = new_user.getRoles().toArray(new Role[0]);
        for (int i = 0; i < new_user.getRoles().size(); i++) {
            roles[i] = objects[i].getRole() + ":" + objects[i].getProject();
        }

        org.springframework.security.core.userdetails.User userToUpdate = new org.springframework.security.core.userdetails.User(new_user.getUsername(), BCrypt.hashpw(new_user.getPassword(), BCrypt.gensalt(12)), new_user.isEnabled(), true, true, true, AuthorityUtils.createAuthorityList(roles));
        userDetailsManager.updateUser(userToUpdate);
        return userRepository.save(new_user);
    }

    @Override
    public Iterable<User> query() {
        return userRepository.findAll();
    }

    @Override
    public User query(String username) {
        log.trace("Looking for user: " + username);
        return userRepository.findFirstByUsername(username);
    }
}
