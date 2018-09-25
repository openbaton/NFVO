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

package org.openbaton.catalogue.nfvo.viminstances;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.images.AWSImage;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.networks.AWSNetwork;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;

@Entity
public class AmazonVimInstance extends BaseVimInstance {
  private String vpcName;

  private String vpcId;

  @NotNull private String accessKey;
  @NotNull private String secretKey;

  private String keyPair;

  private String region;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<AWSImage> images;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<AWSNetwork> networks;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> securityGroups;

  @OneToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.ALL})
  private Set<DeploymentFlavour> flavours;

  @Override
  public Set<? extends BaseNfvImage> getImages() {
    return images;
  }

  @Override
  public Set<? extends BaseNetwork> getNetworks() {
    return networks;
  }

  @Override
  public void addAllNetworks(Collection<BaseNetwork> networks) {
    if (this.networks == null) this.networks = new HashSet<>();
    networks.forEach(n -> this.networks.add((AWSNetwork) n));
  }

  public void addAllFlavours(Collection<DeploymentFlavour> flavours) {
    if (this.flavours == null) this.flavours = new HashSet<>();
    flavours.forEach(i -> this.flavours.add(i));
  }

  @Override
  public void addAllImages(Collection<BaseNfvImage> images) {
    if (this.images == null) this.images = new HashSet<>();
    images.forEach(i -> this.images.add((AWSImage) i));
  }

  public void removeAllNetworks() {
    this.networks.clear();
  }

  public void removeAllImages() {
    this.images.clear();
  }

  public void removeAllFlavours() {
    this.flavours.clear();
  }

  @Override
  public void removeAllNetworks(Collection<BaseNetwork> networks) {}

  @Override
  public void removeAllImages(Collection<BaseNfvImage> images) {}

  @Override
  public void addImage(BaseNfvImage image) {}

  @Override
  public void addNetwork(BaseNetwork network) {}

  public String getVpcName() {
    return vpcName;
  }

  public void setVpcName(String vpcName) {
    this.vpcName = vpcName;
  }

  public String getVpcId() {
    return vpcId;
  }

  public void setVpcId(String vpcId) {
    this.vpcId = vpcId;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getKeyPair() {
    return keyPair;
  }

  public void setKeyPair(String keyPair) {
    this.keyPair = keyPair;
  }

  public void setImages(Set<AWSImage> images) {
    this.images = images;
  }

  public void setNetworks(Set<AWSNetwork> networks) {
    this.networks = networks;
  }

  public Set<String> getSecurityGroups() {
    return securityGroups;
  }

  public void setSecurityGroups(Set<String> securityGroups) {
    this.securityGroups = securityGroups;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  @Override
  public String toString() {
    return "AmazonVimInstance{"
        + "vpcName='"
        + vpcName
        + '\''
        + "vpcId='"
        + vpcId
        + '\''
        + ", accessKey='"
        + accessKey
        + '\''
        + ", keyPair='"
        + keyPair
        + '\''
        + ", securityGroups="
        + securityGroups
        + ", flavours="
        + flavours
        + ", images="
        + images
        + ", networks="
        + networks
        + "} "
        + super.toString();
  }

  public Set<DeploymentFlavour> getFlavours() {
    return flavours;
  }

  public void setFlavours(Set<DeploymentFlavour> flavours) {
    this.flavours = flavours;
  }
}
