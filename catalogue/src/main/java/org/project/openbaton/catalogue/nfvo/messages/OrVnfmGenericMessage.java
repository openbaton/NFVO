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

package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by mob on 14.09.15.
 */
public class OrVnfmGenericMessage implements OrVnfmMessage {
    private Action action;
    private VirtualNetworkFunctionRecord vnfr;
    private VNFRecordDependency vnfrd;

    public OrVnfmGenericMessage(VirtualNetworkFunctionRecord vnfr, Action action) {
        this.vnfr = vnfr;
        this.action=action;
    }
    public VNFRecordDependency getVnfrd() {
        return vnfrd;
    }

    public void setVnfrd(VNFRecordDependency vnfrd) {
        this.vnfrd = vnfrd;
    }

    public VirtualNetworkFunctionRecord getVnfr() {
        return vnfr;
    }

    public void setVnfr(VirtualNetworkFunctionRecord vnfr) {
        this.vnfr = vnfr;
    }

    @Override
    public String toString() {
        return "OrVnfmGenericMessage{" +
                "action=" + action +
                ", vnfr=" + vnfr +
                ", vnfrd=" + vnfrd +
                '}';
    }

    @Override
    public Action getAction() {
        return action;
    }
}
