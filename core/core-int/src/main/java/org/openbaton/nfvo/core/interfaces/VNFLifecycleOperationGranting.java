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

package org.openbaton.nfvo.core.interfaces;

import java.util.Map;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;

/** Created by mpa on 05/05/15. */
public interface VNFLifecycleOperationGranting {

  /**
   * This operation allows requesting the permission to perform a certain VNF lifecycle operation on
   * a new or existing VNF. The sub-type of lifecycle operation is parameterized in the operation.
   */
  Map<String, VimInstance> grantLifecycleOperation(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws VimException, PluginException;
}
