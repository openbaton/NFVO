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

package org.openbaton.nfvo.api.utils;

import org.openbaton.catalogue.security.BaseUser;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.ServiceMetadata;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.UserManagement;
import org.openbaton.nfvo.repositories.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Utils {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private UserManagement userManagement;
  @Autowired private ServiceRepository serviceRepository;

  private ServiceMetadata getCurrentService() throws NotFoundException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new NotFoundException("authentication invalid");
    }
    String currentServiceName = authentication.getName();
    ServiceMetadata serviceMetadata = serviceRepository.findByName(currentServiceName);
    if (serviceMetadata != null) {
      return serviceMetadata;
    } else {
      throw new NotFoundException("Service with name " + currentServiceName + " not found");
    }
  }

  public BaseUser getCurrentUser() throws NotFoundException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new NotFoundException("authentication invalid");
    }
    String currentUserName = authentication.getName();
    User user;
    try {
      user = userManagement.queryByName(currentUserName);
    } catch (NotFoundException e) {
      return serviceRepository.findByName(currentUserName);
    }
    return user;
  }

  public boolean isAdmin() throws NotFoundException {
    BaseUser currentUser = getCurrentUser();

    log.trace("Check user if admin: " + currentUser.getId());
    for (Role role : currentUser.getRoles()) {
      if (role.getRole().ordinal() == Role.RoleEnum.ADMIN.ordinal()) {
        return true;
      }
    }
    return false;
  }
}
