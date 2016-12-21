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

package org.openbaton.catalogue.nfvo.messages;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;
import org.openbaton.catalogue.security.Key;

/** Created by mob on 14.09.15. */
public class OrVnfmInstantiateMessage extends OrVnfmMessage {
  private VirtualNetworkFunctionDescriptor vnfd;
  private VNFDeploymentFlavour vnfdf;
  private String vnfInstanceName;
  private Set<VirtualLinkRecord> vlrs;
  private Map<String, String> extension;
  private Map<String, Collection<VimInstance>> vimInstances;
  private VNFPackage vnfPackage;

  public Set<Key> getKeys() {
    return keys;
  }

  public void setKeys(Set<Key> keys) {
    this.keys = keys;
  }

  private Set<Key> keys;

  public OrVnfmInstantiateMessage() {
    this.action = Action.INSTANTIATE;
  }

  public OrVnfmInstantiateMessage(
      VirtualNetworkFunctionDescriptor vnfd,
      VNFDeploymentFlavour vnfdf,
      String vnfInstanceName,
      Set<VirtualLinkRecord> vlrs,
      Map<String, String> extension,
      Map<String, Collection<VimInstance>> vimInstances,
      Set<Key> keys,
      VNFPackage vnfPackage) {
    this.vnfd = vnfd;
    this.keys = keys;
    this.vnfdf = vnfdf;
    this.vnfInstanceName = vnfInstanceName;
    this.vlrs = vlrs;
    this.extension = extension;
    this.vimInstances = vimInstances;
    this.action = Action.INSTANTIATE;
    this.vnfPackage = vnfPackage;
  }

  public Map<String, Collection<VimInstance>> getVimInstances() {
    return vimInstances;
  }

  public void setVimInstances(Map<String, Collection<VimInstance>> vimInstances) {
    this.vimInstances = vimInstances;
  }

  public VNFPackage getVnfPackage() {
    return vnfPackage;
  }

  public void setVnfPackage(VNFPackage vnfPackage) {
    this.vnfPackage = vnfPackage;
  }

  public VirtualNetworkFunctionDescriptor getVnfd() {
    return vnfd;
  }

  public void setVnfd(VirtualNetworkFunctionDescriptor vnfd) {
    this.vnfd = vnfd;
  }

  public VNFDeploymentFlavour getVnfdf() {
    return vnfdf;
  }

  public void setVnfdf(VNFDeploymentFlavour vnfdf) {
    this.vnfdf = vnfdf;
  }

  public String getVnfInstanceName() {
    return vnfInstanceName;
  }

  public void setVnfInstanceName(String vnfInstanceName) {
    this.vnfInstanceName = vnfInstanceName;
  }

  public Set<VirtualLinkRecord> getVlrs() {
    return vlrs;
  }

  public void setVlrs(Set<VirtualLinkRecord> vlrs) {
    this.vlrs = vlrs;
  }

  public Map<String, String> getExtension() {
    return extension;
  }

  public void setExtension(Map<String, String> extension) {
    this.extension = extension;
  }

  @Override
  public String toString() {
    String result =
        "OrVnfmInstantiateMessage{"
            + "vnfd="
            + vnfd
            + ", vnfdf="
            + vnfdf
            + ", vnfInstanceName='"
            + vnfInstanceName
            + '\''
            + ", vlrs="
            + vlrs
            + ", vimInstances="
            + vimInstances;
    if (vnfPackage != null) {
      result += ", vnfPackage=" + vnfPackage.getName();
    } else {
      result += ", vnfPackage=" + vnfPackage;
    }
    result += ", extension=" + extension + '}';
    return result;
  }
}
