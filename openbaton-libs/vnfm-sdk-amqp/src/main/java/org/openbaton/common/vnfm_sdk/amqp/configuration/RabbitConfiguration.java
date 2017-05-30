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

package org.openbaton.common.vnfm_sdk.amqp.configuration;

import org.openbaton.common.vnfm_sdk.interfaces.LogDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/** Created by lto on 09/11/15. */
// TODO consider removing this class once the new vnfm registration and communication stuff is stable
public class RabbitConfiguration {

  public static final String queueName_vnfmCoreActions = "vnfm.nfvo.actions";
  public static final String queueName_vnfmCoreActionsReply = "vnfm.nfvo.actions.reply";
  private static final String queueName_logDispatch = "nfvo.vnfm.logs";


}
