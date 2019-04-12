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

package org.openbaton.catalogue.api;

import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.security.Key;

public class DeployNSRBody {

  private Set<Key> keys;
  private Map<String, Configuration> configurations;
  private Map<String, Set<String>> vduVimInstances;

  public void setVduVimInstances(Map<String, Set<String>> vduVimInstances) {
    this.vduVimInstances = vduVimInstances;
  }

  public Map<String, Set<String>> getVduVimInstances() {
    return vduVimInstances;
  }

  public Map<String, Configuration> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Map<String, Configuration> configurations) {
    this.configurations = configurations;
  }

  public Set<Key> getKeys() {
    return keys;
  }

  public void setKeys(Set<Key> keys) {
    this.keys = keys;
  }
}
