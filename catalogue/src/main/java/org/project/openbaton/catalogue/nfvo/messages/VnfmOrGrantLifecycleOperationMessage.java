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

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

import java.util.Set;

/**
 * Created by mob on 15.09.15.
 */
public class VnfmOrGrantLifecycleOperationMessage implements VnfmOrMessage{
    private VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;
    private Set<VirtualDeploymentUnit> vduSet;
    private String deploymentFlavourKey;

    public VnfmOrGrantLifecycleOperationMessage(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, Set<VirtualDeploymentUnit> vduSet, String deploymentFlavourKey) {

        this.virtualNetworkFunctionDescriptor = virtualNetworkFunctionDescriptor;
        this.vduSet = vduSet;
        this.deploymentFlavourKey = deploymentFlavourKey;
    }

    public VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor() {
        return virtualNetworkFunctionDescriptor;
    }

    public void setVirtualNetworkFunctionDescriptor(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor) {
        this.virtualNetworkFunctionDescriptor = virtualNetworkFunctionDescriptor;
    }

    public Set<VirtualDeploymentUnit> getVduSet() {
        return vduSet;
    }

    public void setVduSet(Set<VirtualDeploymentUnit> vduSet) {
        this.vduSet = vduSet;
    }

    public String getDeploymentFlavourKey() {
        return deploymentFlavourKey;
    }

    public void setDeploymentFlavourKey(String deploymentFlavourKey) {
        this.deploymentFlavourKey = deploymentFlavourKey;
    }

    @Override
    public String toString() {
        return "VnfmOrGrantLifecycleOperationMessage{" +
                "virtualNetworkFunctionDescriptor='" + virtualNetworkFunctionDescriptor + '\'' +
                ", vduSet=" + vduSet +
                ", deploymentFlavourKey='" + deploymentFlavourKey + '\'' +
                '}';
    }

    @Override
    public Action getAction() {
        return Action.GRANT_OPERATION;
    }
}
