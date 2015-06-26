package org.project.neutrino.nfvo.gui;

import java.util.ArrayList;
import java.util.List;

import org.project.neutrino.nfvo.catalogue.security.User;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class QuickPollUserDetailsService implements UserDetailsService {
	@Autowired
    @Qualifier("userRepository")
	private GenericRepository<User> userRepository;
//	private UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		User authUser = null;
		for(User user : userRepository.findAll())
		{
			if(user.getUsername().equals(username)){
				authUser=user;
				break;
			}
		}
		if (authUser == null) {
			throw new UsernameNotFoundException(String.format(
					"User	with	the	username	%s	doesn't	exist", username));
		}
		// Create a granted authority based on user's role.
		// Can't pass null authorities to user. Hence initialize with an empty
		// arraylist
		List<GrantedAuthority> authorities = new ArrayList<>();
		if (authUser.isAdmin()) {
			authorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN");
		}
		// Create a UserDetails object from the data
		UserDetails userDetails = new org.springframework.security.core.userdetails.User(
				authUser.getUsername(), authUser.getPassword(), authorities);
		return userDetails;
	}
}
