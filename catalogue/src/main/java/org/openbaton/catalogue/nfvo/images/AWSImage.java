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

package org.openbaton.catalogue.nfvo.images;

import javax.persistence.Entity;

@Entity
public class AWSImage extends BaseNfvImage {
  private String name;
  private String description;
  private String hypervisor;
  private String imageOwner;
  private boolean isPublic;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getHypervisor() {
    return hypervisor;
  }

  public void setHypervisor(String hypervisor) {
    this.hypervisor = hypervisor;
  }

  public String getImageOwner() {
    return imageOwner;
  }

  public void setImageOwner(String imageOwner) {
    this.imageOwner = imageOwner;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean aPublic) {
    isPublic = aPublic;
  }

  @Override
  public String toString() {
    return "AWSImage{"
        + "name='"
        + name
        + '\''
        + ", hypervisor="
        + hypervisor
        + '\''
        + ", imageOwner='"
        + imageOwner
        + '\''
        + ", description='"
        + description
        + '\''
        + "} "
        + super.toString();
  }
}
