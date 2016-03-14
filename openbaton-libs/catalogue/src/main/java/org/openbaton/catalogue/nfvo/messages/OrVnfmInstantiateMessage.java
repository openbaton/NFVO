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

import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

import java.util.Map;
import java.util.Set;

/**
 * Created by mob on 14.09.15.
 */
public class OrVnfmInstantiateMessage extends OrVnfmMessage {
    private VirtualNetworkFunctionDescriptor vnfd;
    private VNFDeploymentFlavour vnfdf;
    private String vnfInstanceName;
    private Set<VirtualLinkRecord> vlrs;
    private Map<String, String> extension;

    public Set<VimInstance> getVimInstances() {
        return vimInstances;
    }

    public void setVimInstances(Set<VimInstance> vimInstances) {
        this.vimInstances = vimInstances;
    }

    private Set<VimInstance> vimInstances;
    private VNFPackage vnfPackage;

    public OrVnfmInstantiateMessage() {
        this.action = Action.INSTANTIATE;
    }

    public OrVnfmInstantiateMessage(VirtualNetworkFunctionDescriptor vnfd, VNFDeploymentFlavour vnfdf, String vnfInstanceName, Set<VirtualLinkRecord> vlrs, Map<String, String> extension, Set<VimInstance> vimInstances, VNFPackage vnfPackage) {
        this.vnfd = vnfd;
        this.vnfdf = vnfdf;
        this.vnfInstanceName = vnfInstanceName;
        this.vlrs = vlrs;
        this.extension = extension;
        this.vimInstances = vimInstances;
        this.action = Action.INSTANTIATE;
        this.vnfPackage = vnfPackage;
    }

    public VNFPackage getVnfPackage() {
        return vnfPackage;
    }

    public void setVnfPackage(VNFPackage vnfPackage) {
        this.vnfPackage = vnfPackage;
    }


    public VirtualNetworkFunctionDescriptor getVnfd() {
        return vnfd;
    }

    public void setVnfd(VirtualNetworkFunctionDescriptor vnfd) {
        this.vnfd = vnfd;
    }

    public VNFDeploymentFlavour getVnfdf() {
        return vnfdf;
    }

    public void setVnfdf(VNFDeploymentFlavour vnfdf) {
        this.vnfdf = vnfdf;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public void setVnfInstanceName(String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
    }

    public Set<VirtualLinkRecord> getVlrs() {
        return vlrs;
    }

    public void setVlrs(Set<VirtualLinkRecord> vlrs) {
        this.vlrs = vlrs;
    }

    public Map<String, String> getExtension() {
        return extension;
    }

    public void setExtension(Map<String, String> extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        String result = "OrVnfmInstantiateMessage{" +
                "vnfd=" + vnfd +
                ", vnfdf=" + vnfdf +
                ", vnfInstanceName='" + vnfInstanceName + '\'' +
                ", vlrs=" + vlrs +
                ", vimInstances=" + vimInstances;
        if (vnfPackage != null) result += ", vnfPackage=" + vnfPackage.getName();
        else result += ", vnfPackage=" + vnfPackage;
        result += ", extension=" + extension + '}';
        return result;
    }
}
