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

package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by lorenzo on 5/30/15.
 */
public class CoreMessage implements Serializable{

    private Action action;
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;


    private VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;

    private VNFRecordDependency dependency;


    private Map<String,String> extension;

    public VNFRecordDependency getDependency() {
        return dependency;
    }

    public void setDependency(VNFRecordDependency dependency) {
        this.dependency = dependency;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor() {
        return virtualNetworkFunctionDescriptor;
    }

    public void setVirtualNetworkFunctionDescriptor(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor) {
        this.virtualNetworkFunctionDescriptor = virtualNetworkFunctionDescriptor;
    }

    public Map<String, String> getExtention() {
        return extension;
    }

    public void setExtension(Map<String, String> extention) {
        this.extension = extention;
    }

    @Override
    public String toString() {
        return "CoreMessage{" +
                "action=" + action +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                ", virtualNetworkFunctionDescriptor=" + virtualNetworkFunctionDescriptor +
                ", dependency=" + dependency +
                ", extension=" + extension +
                '}';
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }
}
