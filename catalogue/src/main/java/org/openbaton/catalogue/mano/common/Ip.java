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

package org.openbaton.catalogue.mano.common;

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 23/09/15. */
@Entity
public class Ip extends BaseEntity {

  private String netName;
  private String ip;

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getNetName() {
    return netName;
  }

  public void setNetName(String netName) {
    this.netName = netName;
  }

  @Override
  public String toString() {
    return "Ip{" + "netName='" + netName + '\'' + ", ip='" + ip + '\'' + "} " + super.toString();
  }
}
