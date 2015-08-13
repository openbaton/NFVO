/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

package org.project.openbaton.vnfm.interfaces.manager;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmManager {
    void init();

    Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException;

    VnfmSender getVnfmSender(EndpointType endpointType);

    void executeAction(CoreMessage message) throws VimException, NotFoundException;

    @Async
    Future<Void> modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, CoreMessage coreMessage) throws NotFoundException;

    Future<Void> release(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException;
}
