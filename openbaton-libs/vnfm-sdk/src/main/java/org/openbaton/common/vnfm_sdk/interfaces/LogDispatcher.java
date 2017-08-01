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

package org.openbaton.common.vnfm_sdk.interfaces;

import org.openbaton.catalogue.nfvo.messages.OrVnfmLogMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrLogMessage;

/** Created by lto on 17/05/16. */
public interface LogDispatcher {

  /**
   * Returns a VnfmOrLogMessage object containing the logging information that was requested in the
   * request parameter.
   *
   * @param request the OrVnfmLogMessage containing the request
   * @return the request's reply encapsulated in a VnfmOrLogMessage
   */
  VnfmOrLogMessage getLogs(OrVnfmLogMessage request);
}
