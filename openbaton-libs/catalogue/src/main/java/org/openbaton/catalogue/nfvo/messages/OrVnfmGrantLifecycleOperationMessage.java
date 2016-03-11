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
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

import java.util.Map;

/**
 * Created by mob on 15.09.15.
 */
public class OrVnfmGrantLifecycleOperationMessage extends OrVnfmMessage{
    private boolean grantAllowed;
    private Map<String, VimInstance> vduVim;

    @Override
    public String toString() {
        return "OrVnfmGrantLifecycleOperationMessage{" +
                "grantAllowed=" + grantAllowed +
                ", vduVim=" + vduVim +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                '}';
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    public OrVnfmGrantLifecycleOperationMessage() {
        this.action = Action.GRANT_OPERATION;
    }

    public boolean isGrantAllowed() {
        return grantAllowed;
    }

    public void setGrantAllowed(boolean grantAllowed) {
        this.grantAllowed = grantAllowed;
    }

    public Map<String, VimInstance> getVduVim() {
        return vduVim;
    }

    public void setVduVim(Map<String, VimInstance> vduVim) {
        this.vduVim = vduVim;
    }
}
