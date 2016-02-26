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

package org.openbaton.catalogue.mano.common.monitoring;

/**
 * Created by mob on 27.10.15.
 */

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import java.io.Serializable;

@Entity
public class AlarmEndpoint implements Serializable {
    @Id
    private String id;
    @Version
    private int version = 0;

    private String name;

    private String resourceId;

    private EndpointType type;
    private String endpoint;

    private PerceivedSeverity perceivedSeverity;

    public AlarmEndpoint() {
    }

    public AlarmEndpoint(String name, String resourceId, EndpointType type, String endpoint, PerceivedSeverity perceivedSeverity) {
        this.name = name;
        this.resourceId = resourceId;
        this.type = type;
        this.endpoint = endpoint;
        this.perceivedSeverity = perceivedSeverity;
    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public PerceivedSeverity getPerceivedSeverity() {
        return perceivedSeverity;
    }

    public void setPerceivedSeverity(PerceivedSeverity perceivedSeverity) {
        this.perceivedSeverity = perceivedSeverity;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EndpointType getType() {
        return type;
    }

    public void setType(EndpointType type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "AlarmEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", type=" + type +
                ", endpoint='" + endpoint + '\'' +
                ", perceivedSeverity=" + perceivedSeverity +
                '}';
    }
}
