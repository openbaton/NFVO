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

package org.openbaton.nfvo.security.interfaces;

import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.NotAllowedException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PasswordWeakException;

/**
 * Created by mpa on 30/04/15.
 */
public interface UserManagement {

  /**
   *
   * @param user
   */
  User add(User user)
      throws PasswordWeakException, NotAllowedException, BadRequestException, NotFoundException;

  /**
   *
   * @param user
   */
  void delete(User user) throws NotAllowedException;

  /**
   *
   * @param new_user
   */
  User update(User new_user) throws NotAllowedException, BadRequestException, NotFoundException;

  /**
   */
  Iterable<User> query();

  /**
   *
   * @param username
   */
  User queryByName(String username) throws NotFoundException;

  /**
   *
   * @param id
   */
  User query(String id) throws NotFoundException;

  /**
   *
   * @param oldPwd
   * @param newPwd
   */
  void changePassword(String oldPwd, String newPwd) throws PasswordWeakException;
}
