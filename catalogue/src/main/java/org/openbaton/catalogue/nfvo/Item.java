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

package org.openbaton.catalogue.nfvo;

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 05/08/15. */
@Entity
public class Item extends BaseEntity {

  private String metric;

  private String hostId;

  private String hostname;
  private String lastValue;
  private String value;

  public Item() {}

  @Override
  public String toString() {
    return "Item{"
        + "metric='"
        + metric
        + '\''
        + ", hostId='"
        + hostId
        + '\''
        + ", hostname='"
        + hostname
        + '\''
        + ", lastValue='"
        + lastValue
        + '\''
        + ", value='"
        + value
        + '\''
        + "} "
        + super.toString();
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getHostId() {
    return hostId;
  }

  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getLastValue() {
    return lastValue;
  }

  public void setLastValue(String lastValue) {
    this.lastValue = lastValue;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
