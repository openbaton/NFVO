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

package org.openbaton.vnfm.interfaces.manager;

import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.openbaton.vnfm.interfaces.tasks.AbstractTask;
import org.springframework.beans.BeansException;

/** Created by lto on 29.05.17. */
public interface MessageGenerator {
  VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException;

  VnfmSender getVnfmSender(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException;

  Map<String, String> getExtension(String monitoringIp);

  Map<String, String> getExtension();

  NFVMessage getNextMessage(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord);

  OrVnfmInstantiateMessage getNextMessage(
      VirtualNetworkFunctionDescriptor vnfd,
      Map<String, Set<String>> vduVimInstances,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body,
      String monitoringIp)
      throws NotFoundException;

  VnfmManagerEndpoint getEndpoint(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException;

  VnfmManagerEndpoint getVnfm(String endpoint) throws NotFoundException;

  VirtualNetworkFunctionRecord setupTask(NFVMessage nfvMessage, AbstractTask task);
}
