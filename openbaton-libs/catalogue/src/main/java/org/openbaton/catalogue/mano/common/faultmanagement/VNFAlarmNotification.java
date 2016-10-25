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

package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;

/**
 * Created by mob on 27.10.15.
 */
public class VNFAlarmNotification extends AbstractVNFAlarm {
  private VNFAlarm vnfAlarm;

  public VNFAlarmNotification(String vnfrId, String faultManagementPolicyId, VNFAlarm vnfAlarm) {

    super(vnfrId, faultManagementPolicyId);
    this.vnfAlarm = vnfAlarm;
  }

  public Alarm getAlarm() {
    return vnfAlarm;
  }

  @Override
  public String toString() {
    return "VNFAlarmNotification{" + "vnfAlarm=" + vnfAlarm + "} " + super.toString();
  }
}
