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
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.openbaton.catalogue.nfvo.Location;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "projectId"}))
public abstract class BaseVimInstance extends BaseEntity {

  @NotNull
  @Size(min = 1)
  private String name;

  @NotNull
  @Size(min = 1)
  private String authUrl;

  public String getAuthUrl() {
    return authUrl;
  }

  public void setAuthUrl(String authUrl) {
    this.authUrl = authUrl;
  }

  public abstract Set<? extends BaseNfvImage> getImages();

  public abstract Set<? extends BaseNetwork> getNetworks();

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Location location;

  private String type;

  private Boolean active = true;

  public BaseVimInstance() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "BaseVimInstance{"
        + "name='"
        + name
        + '\''
        + ", location="
        + location
        + ", type='"
        + type
        + '\''
        + ", active="
        + active
        + "} "
        + super.toString();
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public Boolean isActive() {
    return active;
  }

  public abstract void addAllNetworks(Collection<BaseNetwork> networks);

  public abstract void addAllImages(Collection<BaseNfvImage> images);

  public abstract void removeAllNetworks(Collection<BaseNetwork> networks);

  public abstract void removeAllImages(Collection<BaseNfvImage> images);

  public abstract void addImage(BaseNfvImage image);

  public abstract void addNetwork(BaseNetwork network);
}
