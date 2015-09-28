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

package org.project.openbaton.catalogue.mano.record;

import org.project.openbaton.catalogue.nfvo.DependencyParameters;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by lto on 08/06/15.
 */
@Entity
public class VNFRecordDependency implements Serializable {

    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    //@OneToOne(cascade = {CascadeType.REFRESH/*, CascadeType.MERGE*/}, fetch = FetchType.EAGER)
    private String target;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<String, DependencyParameters> parameters;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, String> idType;

//    @Enumerated(EnumType.STRING)
//    private Status status;

    public VNFRecordDependency() {
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }

    public Map<String, String> getIdType() {
        return idType;
    }

    public void setIdType(Map<String, String> idType) {
        this.idType = idType;
    }

    @Override
    public String toString() {
        return "VNFRecordDependency{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", target="+target+
                ", parameters=" + parameters +
                ", idType=" + idType +
                '}';
    }

    public Map<String, DependencyParameters> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, DependencyParameters> parameters) {
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

//    public Status getStatus() {
//        return status;
//    }
//
//    public void setStatus(Status status) {
//        this.status = status;
//    }
}