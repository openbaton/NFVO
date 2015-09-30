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

import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

import java.util.Map;
import java.util.Set;

/**
 * Created by mob on 14.09.15.
 */
public class OrVnfmInstantiateMessage extends OrVnfmMessage {
    private Set<Script> scripts;
    private String scriptsLink;
    private VirtualNetworkFunctionDescriptor vnfd;
    private VNFDeploymentFlavour vnfdf;
    private String vnfInstanceName;
    private Set<VirtualLinkRecord> vlrs;
    private Map<String, String> extention;

    public OrVnfmInstantiateMessage() {
        this.action = Action.INSTANTIATE;
    }

    public OrVnfmInstantiateMessage(VirtualNetworkFunctionDescriptor vnfd, VNFDeploymentFlavour vnfdf, String vnfInstanceName, Set<VirtualLinkRecord> vlrs, Map<String, String> extention) {
        this.vnfd = vnfd;
        this.vnfdf = vnfdf;
        this.vnfInstanceName = vnfInstanceName;
        this.vlrs = vlrs;
        this.extention = extention;
        this.scripts = null;
        this.action = Action.INSTANTIATE;
    }

    public OrVnfmInstantiateMessage(VirtualNetworkFunctionDescriptor vnfd, VNFDeploymentFlavour vnfdf, String vnfInstanceName, Set<VirtualLinkRecord> vlrs, Map<String, String> extention, String scriptLink) {
        this(vnfd, vnfdf, vnfInstanceName, vlrs, extention);
        this.scriptsLink = scriptLink;
    }

    public OrVnfmInstantiateMessage(VirtualNetworkFunctionDescriptor vnfd, VNFDeploymentFlavour deploymentFlavour, String name, Set<VirtualLinkRecord> vlr, Map<String, String> extension, Set<Script> scripts) {
        this(vnfd, deploymentFlavour, name, vlr, extension);
        this.scripts = scripts;
    }

    public Set<Script> getScripts() {
        return scripts;
    }

    public void setScripts(Set<Script> scripts) {
        this.scripts = scripts;
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

    public Map<String, String> getExtention() {
        return extention;
    }

    public void setExtention(Map<String, String> extention) {
        this.extention = extention;
    }

    @Override
    public String toString() {
        return "OrVnfmInstantiateMessage{" +
                "vnfd=" + vnfd +
                ", vnfdf=" + vnfdf +
                ", vnfInstanceName='" + vnfInstanceName + '\'' +
                ", vlrs=" + vlrs +
                ", extention=" + extention +
                '}';
    }


    public String getScriptsLink() {
        return scriptsLink;
    }

    public void setScriptsLink(String link) {
        scriptsLink = link;
    }
}
