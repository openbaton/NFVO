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

package org.openbaton.catalogue.nfvo;

import org.openbaton.catalogue.mano.record.VNFRecordDependency;

import java.util.List;

/**
 * Created by lto on 19/08/15.
 */
public class QueueElement {

    private String vnfrTargetId;

    private List<VNFRecordDependency> dependencies;

    private int waitingFor;

    public String getVnfrTargetId() {
        return vnfrTargetId;
    }

    public void setVnfrTargetId(String vnfrTargetId) {
        this.vnfrTargetId = vnfrTargetId;
    }

    public List<VNFRecordDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<VNFRecordDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public int getWaitingFor() {
        return waitingFor;
    }

    public void setWaitingFor(int waitingFor) {
        this.waitingFor = waitingFor;
    }

    @Override
    public String toString() {
        return "QueueElement{" +
                "vnfrTargetId='" + vnfrTargetId + '\'' +
                ", dependencies=" + dependencies +
                ", waitingFor=" + waitingFor +
                '}';
    }
}
