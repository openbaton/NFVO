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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;

@Entity
public class GenericVimInstance extends BaseVimInstance {

  public void setImages(Set<BaseNfvImage> images) {
    this.images = images;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<BaseNfvImage> images;

  public void setNetworks(Set<BaseNetwork> networks) {
    this.networks = networks;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<BaseNetwork> networks;

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
    networks.forEach(n -> this.networks.add(n));
  }

  @Override
  public void addAllImages(Collection<BaseNfvImage> images) {
    if (this.images == null) this.images = new HashSet<>();
    images.forEach(image -> this.images.add(image));
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
  public void addImage(BaseNfvImage image) {
    this.images.add(image);
  }

  @Override
  public void addNetwork(BaseNetwork network) {
    this.networks.add(network);
  }
}
