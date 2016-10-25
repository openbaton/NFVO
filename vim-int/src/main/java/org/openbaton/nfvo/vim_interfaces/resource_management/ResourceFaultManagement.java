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

package org.openbaton.nfvo.vim_interfaces.resource_management;

import org.openbaton.catalogue.mano.common.monitoring.Alarm;

import java.util.List;

/**
 * Created by mpa on 30/04/15.
 */
public interface ResourceFaultManagement {

  /**
   * This operation enables the NFVO to subscribe for notifications related to the alarms and their
   * state changes resulting from the virtualised resources faults with the VIM. This also enables
   * the NFVO to specify the scope of the subscription in terms of the specific alarms for the
   * virtualised resources to be reported by the VIM using a filter as the input.
   */
  void subscribe();

  /**
   * This operation distributes notifications to subscribers. It is a one-way operation issued by
   * the VIM that cannot be invoked as an operation by the consumer (NFVO). In order to receive
   * notifications, the NFVO shall have a subscription.
   */
  void notifyInformation();

  /**
   * This operation enables the NFVOs to query for active alarms from the VIM.
   */
  List<Alarm> getAlarmList();
}
