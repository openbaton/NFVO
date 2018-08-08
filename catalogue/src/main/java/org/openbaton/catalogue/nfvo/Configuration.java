/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.catalogue.nfvo;

/** Created by lto on 18/05/15. */
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
public class Configuration extends BaseEntity {

  // TODO think at cascade type
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ConfigurationParameter> configurationParameters;

  private String name;

  @Override
  public String toString() {
    return "Configuration{"
        + "configurationParameters="
        + configurationParameters
        + ", name='"
        + name
        + '\''
        + "} "
        + super.toString();
  }

  public Configuration() {}

  public Configuration(String name) {
    this.name = name;
    this.configurationParameters = new HashSet<>();
  }

  public Set<ConfigurationParameter> getConfigurationParameters() {
    return configurationParameters;
  }

  public void setConfigurationParameters(Set<ConfigurationParameter> configurationParameters) {
    this.configurationParameters = configurationParameters;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
