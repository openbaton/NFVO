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
 * Created by lto on 20/05/15.
 */
@Entity
public class Quota implements Serializable{
    @Id
    private String id;
    @Version
    private int version = 0;

    private String tenant;

    private int cores;
    private int floatingIps;
    private int instances;
    private int keyPairs;
    private int ram;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public int getKeyPairs() {
        return keyPairs;
    }

    public void setKeyPairs(int keyPairs) {
        this.keyPairs = keyPairs;
    }

    public int getInstances() { return instances; }

    public void setInstances(int instances) { this.instances = instances; }

    public int getFloatingIps() {
        return floatingIps;
    }

    public void setFloatingIps(int floatingIps) {
        this.floatingIps = floatingIps;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Quota{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", tenant='" + tenant + '\'' +
                ", cores='" + cores + '\'' +
                ", floatingIps='" + floatingIps + '\'' +
                ", instances='" + instances + '\'' +
                ", keypairs='" + keyPairs + '\'' +
                ", ram='" + ram + '\'' +
                '}';
    }
}

