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

package org.openbaton.catalogue.nfvo;

import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.PrePersist;
import org.openbaton.catalogue.util.BaseEntity;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mob on 03.05.17. */
@Entity
public class AdditionalRepoInfo extends BaseEntity {
  private PackageType packageType;
  private String keyUrl;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> configuration;

  public AdditionalRepoInfo() {}

  public AdditionalRepoInfo(PackageType packageType) {
    this.packageType = packageType;
  }

  public PackageType getPackageType() {
    return packageType;
  }

  public void setPackageType(PackageType packageType) {
    this.packageType = packageType;
  }

  public String getKeyUrl() {
    return keyUrl;
  }

  public void setKeyUrl(String keyUrl) {
    if (keyUrl == null) throw new NullPointerException("KeyUrl is null");
    this.keyUrl = keyUrl;
  }

  @PrePersist
  public void ensureId() {
    setId(IdGenerator.createUUID());
  }

  public Set<String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Set<String> configuration) {
    if (configuration == null) throw new NullPointerException("The configuration list is null");
    this.configuration = configuration;
  }

  @Override
  public String toString() {
    return "AdditionalRepoInfo{"
        + "packageType="
        + packageType
        + ", keyUrl='"
        + keyUrl
        + '\''
        + ", configuration="
        + configuration
        + "} "
        + super.toString();
  }
}
