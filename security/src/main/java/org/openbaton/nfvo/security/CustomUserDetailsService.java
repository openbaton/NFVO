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

package org.openbaton.nfvo.security;

import org.openbaton.catalogue.security.User;
import org.openbaton.nfvo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomUserDetailsService implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

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

