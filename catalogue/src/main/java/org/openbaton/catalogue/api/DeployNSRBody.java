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

package org.openbaton.catalogue.api;

import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.security.Key;

import java.util.Collection;
import java.util.Map;

/**
 * Created by lto on 10/08/16.
 */
public class DeployNSRBody {
  private Map<String, Collection<String>> vduVimInstances;
  private Collection<Key> keys;
  private Map<String, Configuration> configurations;

  public Map<String, Collection<String>> getVduVimInstances() {
    return vduVimInstances;
  }

  public void setVduVimInstances(Map<String, Collection<String>> vduVimInstances) {
    this.vduVimInstances = vduVimInstances;
  }

  public Map<String, Configuration> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Map<String, Configuration> configurations) {
    this.configurations = configurations;
  }

  public Collection<Key> getKeys() {
    return keys;
  }

  public void setKeys(Collection<Key> keys) {
    this.keys = keys;
  }
}
