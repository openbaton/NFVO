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

package org.openbaton.common.vnfm_sdk;

import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 23/09/15. */
public abstract class VnfmHelper {

  protected Logger log = LoggerFactory.getLogger(this.getClass());

  public abstract void sendToNfvo(NFVMessage nfvMessage);

  public abstract NFVMessage sendAndReceive(NFVMessage nfvMessage) throws Exception;

  public abstract String sendAndReceive(String message, String queueName) throws Exception;
}
