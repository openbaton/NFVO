package org.openbaton.nfvo.security.authorization;

import org.openbaton.catalogue.security.User;
import org.openbaton.nfvo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 25/02/16.
 */
@Service
public class UserManagement implements org.openbaton.nfvo.security.interfaces.UserManagement {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User add(User user) {
//        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt(12)));
        return userRepository.save(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public User update(User new_user) {
        return userRepository.save(new_user);
    }

    @Override
    public Iterable<User> query() {
        return userRepository.findAll();
    }

    @Override
    public User query(String username) {
        return userRepository.findFirstByUsername(username);
    }
}
