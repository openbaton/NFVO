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

package org.openbaton.vnfm.interfaces.state;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.springframework.scheduling.annotation.Async;

/** Created by lto on 29.05.17. */
public interface VnfStateHandler {
  Future<Void> handleVNF(
      NetworkServiceDescriptor networkServiceDescriptor,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body,
      Map<String, Set<String>> vduVimInstances,
      VirtualNetworkFunctionDescriptor vnfd,
      String monitoringIp)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  @Async
  Future<NFVMessage> executeAction(Future<NFVMessage> nfvMessage)
      throws ExecutionException, InterruptedException;

  @Async
  Future<NFVMessage> executeAction(NFVMessage nfvMessage);

  void terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

  @Async
  Future<Void> sendMessageToVNFR(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, NFVMessage nfvMessage)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;
}
