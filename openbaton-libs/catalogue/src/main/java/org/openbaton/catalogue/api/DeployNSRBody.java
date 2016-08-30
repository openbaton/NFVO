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
