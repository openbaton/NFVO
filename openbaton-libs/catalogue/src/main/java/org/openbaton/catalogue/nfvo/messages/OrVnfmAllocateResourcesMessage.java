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

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

import java.util.Set;

/**
 * Created by mob on 15.09.15.
 */
public class OrVnfmAllocateResourcesMessage extends OrVnfmMessage {
    private Set<VirtualDeploymentUnit> vduSet;

    public OrVnfmAllocateResourcesMessage() {
        this.action = Action.ALLOCATE_RESOURCES;
    }

    public OrVnfmAllocateResourcesMessage(Set<VirtualDeploymentUnit> vduSet) {
        this.vduSet = vduSet;
        this.action = Action.ALLOCATE_RESOURCES;
    }

    public Set<VirtualDeploymentUnit> getVduSet() {
        return vduSet;
    }

    public void setVduSet(Set<VirtualDeploymentUnit> vduSet) {
        this.vduSet = vduSet;
    }

    @Override
    public String toString() {
        return "OrVnfmAllocateResourcesMessage{" +
                "vduSet=" + vduSet +
                '}';
    }

}
