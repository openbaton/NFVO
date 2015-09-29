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

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by lto on 05/08/15.
 */
@Entity
public class Item implements Serializable{
    @Id
    private String id;
    @Version
    private int version = 0;

    private String metric;

    private String hostExtId;

    private String vnfcInstanceId;
    private String lastValue;
    private String value;

    public Item() {
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", metric='" + metric + '\'' +
                ", hostExtId='" + hostExtId + '\'' +
                ", vnfcInstanceId='" + vnfcInstanceId + '\'' +
                ", lastValue='" + lastValue + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getHostExtId() {
        return hostExtId;
    }

    public void setHostExtId(String hostExtId) {
        this.hostExtId = hostExtId;
    }

    public String getVnfcInstanceId() {
        return vnfcInstanceId;
    }

    public void setVnfcInstanceId(String vduId) {
        this.vnfcInstanceId = vnfcInstanceId;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
