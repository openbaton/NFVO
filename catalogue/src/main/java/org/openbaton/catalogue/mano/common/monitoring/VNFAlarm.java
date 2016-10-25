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

package org.openbaton.catalogue.mano.common.monitoring;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mob on 21.01.16.
 */
@Entity
public class VNFAlarm extends Alarm {

  private String vnfrId;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> vnfcIds;

  private String vimName;

  public VNFAlarm() {
    this.alarmType = AlarmType.VIRTUAL_NETWORK_FUNCTION;
  }

  @Override
  public AlarmType getAlarmType() {
    return alarmType;
  }

  @Override
  public void setAlarmType(AlarmType alarmType) {
    this.alarmType = alarmType;
  }

  public String getVnfrId() {
    return vnfrId;
  }

  public void setVnfrId(String vnfrId) {
    this.vnfrId = vnfrId;
  }

  public List<String> getVnfcIds() {
    return vnfcIds;
  }

  public void addVnfcId(String vnfcId) {
    if (vnfcIds == null) vnfcIds = new ArrayList<>();
    vnfcIds.add(vnfcId);
  }

  public void setVnfcIds(List<String> vnfcIds) {
    this.vnfcIds = vnfcIds;
  }

  public String getVimName() {
    return vimName;
  }

  public void setVimName(String vimName) {
    this.vimName = vimName;
  }

  @Override
  public String toString() {
    return "VNFAlarm{"
        + "vnfrId='"
        + vnfrId
        + '\''
        + ", vnfcIds="
        + vnfcIds
        + ", vimName='"
        + vimName
        + '\''
        + "} "
        + super.toString();
  }
}
