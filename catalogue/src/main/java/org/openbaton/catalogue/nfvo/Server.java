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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.SubnetIp;
import org.openbaton.catalogue.nfvo.images.NFVImage;

public class Server implements Serializable {
  private String name;

  private NFVImage image;
  private DeploymentFlavour flavor;

  private String status;
  private String extendedStatus;
  private String extId;

  private Map<String, Set<SubnetIp>> ips;

  private Map<String, String> floatingIps;

  private Date created;
  private Date updated;

  private String hostName;
  private String hypervisorHostName;
  private String instanceName;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
  }

  public Map<String, Set<SubnetIp>> getIps() {
    return ips;
  }

  public void setIps(Map<String, Set<SubnetIp>> ips) {
    this.ips = ips;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getExtendedStatus() {
    return extendedStatus;
  }

  public void setExtendedStatus(String extendedStatus) {
    this.extendedStatus = extendedStatus;
  }

  public NFVImage getImage() {
    return image;
  }

  public void setImage(NFVImage image) {
    this.image = image;
  }

  public DeploymentFlavour getFlavor() {
    return flavor;
  }

  public void setFlavor(DeploymentFlavour flavor) {
    this.flavor = flavor;
  }

  public Map<String, String> getFloatingIps() {
    return floatingIps;
  }

  public void setFloatingIps(Map<String, String> floatingIps) {
    this.floatingIps = floatingIps;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getHypervisorHostName() {
    return hypervisorHostName;
  }

  public void setHypervisorHostName(String hypervisorHostName) {
    this.hypervisorHostName = hypervisorHostName;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  @Override
  public String toString() {
    return "Server{"
        + "name='"
        + name
        + '\''
        + ", image="
        + image
        + ", flavor="
        + flavor
        + ", status='"
        + status
        + '\''
        + ", extendedStatus='"
        + extendedStatus
        + '\''
        + ", extId='"
        + extId
        + '\''
        + ", ips="
        + ips
        + ", floatingIps="
        + floatingIps
        + ", created="
        + created
        + ", updated="
        + updated
        + ", hostName='"
        + hostName
        + '\''
        + ", hypervisorHostName='"
        + hypervisorHostName
        + '\''
        + ", instanceName='"
        + instanceName
        + '\''
        + '}';
  }
}
