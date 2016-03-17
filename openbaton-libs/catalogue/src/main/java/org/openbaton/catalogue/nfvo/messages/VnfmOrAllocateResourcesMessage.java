/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

import java.util.Map;

/**
 * Created by mob on 15.09.15.
 */
public class VnfmOrAllocateResourcesMessage extends VnfmOrMessage {
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    private Map<String, VimInstance> vimInstances;

    public VnfmOrAllocateResourcesMessage() {
        this.action = Action.ALLOCATE_RESOURCES;
    }

    @Override
    public String toString() {
        return "VnfmOrAllocateResourcesMessage{" +
                "vimInstances=" + vimInstances +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                '}';
    }

    public Map<String, VimInstance> getVimInstances() {
        return vimInstances;
    }

    public void setVimInstances(Map<String, VimInstance> vimInstances) {
        this.vimInstances = vimInstances;
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }
}