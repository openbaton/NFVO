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

package org.openbaton.catalogue.mano.descriptor;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Created by lto on 06/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 * <p>A policy or rule to apply to the NFP
 */
@Entity
public class Connection implements Serializable {

  @Version private int version = 0;
  @Id private String id;

  private String vnf_name;

  private String virtual_link;

  public Connection() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVnf_name() {
    return vnf_name;
  }

  public void setVnf_name(String vnf_name) {
    this.vnf_name = vnf_name;
  }

  public String getVirtual_link() {
    return virtual_link;
  }

  public void setVirtual_link(String virtual_link) {
    this.virtual_link = virtual_link;
  }

  @Override
  public String toString() {
    return "Connection{"
        + "version="
        + version
        + ", id='"
        + id
        + '\''
        + ", vnf_name='"
        + vnf_name
        + '\''
        + ", virtual_link='"
        + virtual_link
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Connection that = (Connection) o;
    return Objects.equals(vnf_name, that.vnf_name)
        && Objects.equals(virtual_link, that.virtual_link);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vnf_name, virtual_link);
  }
}
