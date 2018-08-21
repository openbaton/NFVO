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

package org.openbaton.catalogue.security;

import javax.persistence.Column;
import javax.persistence.Entity;

/** Created by lto on 04/04/2017. */
@Entity
public class ServiceMetadata extends BaseUser {

  private String keyValue;

  @Column(unique = true)
  private String name;

  // The encrypted token
  private String token;

  private long tokenExpirationDate;

  public String getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(String keyValue) {
    this.keyValue = keyValue;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public long getTokenExpirationDate() {
    return tokenExpirationDate;
  }

  public void setTokenExpirationDate(long tokenExpirationDate) {
    this.tokenExpirationDate = tokenExpirationDate;
  }

  @Override
  public String toString() {
    return "ServiceMetadata{"
        + "key="
        + keyValue
        + ", name="
        + name
        + ", token="
        + token
        + ", tokenExpirationDate="
        + tokenExpirationDate
        + "} "
        + super.toString();
  }
}
