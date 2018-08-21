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
import javax.validation.constraints.Size;
import org.openbaton.catalogue.keys.PopKeypair;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;

@Entity
public class OpenstackVimInstance extends BaseVimInstance {

  private String tenant;

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  private String domain;

  @NotNull
  @Size(min = 1)
  private String username;

  @NotNull
  @Size(min = 1)
  private String password;

  private String keyPair;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> securityGroups;

  // can store a self signed certificate used by the OpenStack instance related to this VIM
  @Column(length = 3700)
  private String openstackSslCertificate;

  @OneToMany(
    fetch = FetchType.EAGER,
    cascade = {CascadeType.ALL}
  )
  private Set<PopKeypair> keys;

  @OneToMany(
    fetch = FetchType.EAGER,
    cascade = {CascadeType.ALL}
  )
  private Set<DeploymentFlavour> flavours;

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getKeyPair() {
    return keyPair;
  }

  public void setKeyPair(String keyPair) {
    this.keyPair = keyPair;
  }

  public Set<String> getSecurityGroups() {
    return securityGroups;
  }

  public void setSecurityGroups(Set<String> securityGroups) {
    this.securityGroups = securityGroups;
  }

  public Set<DeploymentFlavour> getFlavours() {
    return flavours;
  }

  public void setFlavours(Set<DeploymentFlavour> flavours) {
    this.flavours = flavours;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<NFVImage> images;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<Network> networks;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<AvailabilityZone> zones;

  @Override
  public String toString() {
    return "OpenstackVimInstance{"
        + "tenant='"
        + tenant
        + '\''
        + ", username='"
        + username
        + '\''
        + ", password='"
        + password
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

  @Override
  public void addAllNetworks(Collection<BaseNetwork> networks) {
    if (this.networks == null) this.networks = new HashSet<>();
    networks.forEach(n -> this.networks.add((Network) n));
  }

  @Override
  public void addAllImages(Collection<BaseNfvImage> images) {
    if (this.images == null) this.images = new HashSet<>();
    images.forEach(i -> this.images.add((NFVImage) i));
  }

  @Override
  public void removeAllNetworks(Collection<BaseNetwork> networks) {
    this.networks.removeAll(networks);
  }

  @Override
  public void removeAllImages(Collection<BaseNfvImage> images) {
    this.images.removeAll(images);
  }

  @Override
  public Set<? extends BaseNfvImage> getImages() {
    return images;
  }

  public void setImages(Set<NFVImage> images) {
    this.images = images;
  }

  public Set<Network> getNetworks() {
    return networks;
  }

  public void setNetworks(Set<Network> networks) {
    this.networks = networks;
  }

  @Override
  public void addImage(BaseNfvImage image) {
    //TODO check cast
    this.images.add((NFVImage) image);
  }

  @Override
  public void addNetwork(BaseNetwork network) {
    //TODO check cast
    this.networks.add((Network) network);
  }

  public Set<AvailabilityZone> getZones() {
    return zones;
  }

  public void setZones(Set<AvailabilityZone> zones) {
    this.zones = zones;
  }

  public Set<PopKeypair> getKeys() {
    return keys;
  }

  public void setKeys(Set<PopKeypair> keys) {
    this.keys = keys;
  }

  public String getOpenstackSslCertificate() {
    return openstackSslCertificate;
  }

  public void setOpenstackSslCertificate(String openstackSslCertificate) {
    this.openstackSslCertificate = openstackSslCertificate;
  }
}
