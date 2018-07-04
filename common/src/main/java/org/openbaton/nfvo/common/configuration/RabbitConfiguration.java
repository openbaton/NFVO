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

package org.openbaton.nfvo.common.configuration;

public class RabbitConfiguration {

  public static final String QUEUE_DURABLE = "true";
  public static final String QUEUE_AUTODELETE = "true";
  public static final String QUEUE_NAME_MANAGER_REGISTER = "nfvo.manager.handling";
  public static final String QUEUE_NAME_VNFM_CORE_ACTIONS = "vnfm.nfvo.actions";
  public static final String QUEUE_NAME_VNFM_CORE_ACTIONS_REPLY = "vnfm.nfvo.actions.reply";
  public static final String QUEUE_NAME_EVENT_REGISTER = "nfvo.event.register";
  public static final String QUEUE_NAME_EVENT_UNREGISTER = "nfvo.event.unregister";
  public static final String EXCHANGE_NAME_OPENBATON = "openbaton-exchange";
  public static final String EXCHANGE_TYPE_OPENBATON = "topic";
  public static final String EXCHANGE_DURABLE_OPENBATON = "true";

}
