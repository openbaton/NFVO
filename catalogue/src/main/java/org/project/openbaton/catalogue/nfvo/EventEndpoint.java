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

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by lto on 01/07/15.
 */

/**
 * This class represents a Event Endpoint. When an external application want to receive events regarding a particular
 * entity it is necessary to send this object to the right URL or queue.
 *
 * This object contains:
 *  * name) must be unique, used for removing a event
 *  * type)
 */
@Entity
public class EventEndpoint implements Serializable{
    @Id
    private String id;
    @Version
    private int version = 0;

    private String name;

    private String networkServiceId;
    private String virtualNetworkFunctionId;

    private EndpointType type;
    private String endpoint;
    private Action event;

    public String getNetworkServiceId() {
        return networkServiceId;
    }

    public void setNetworkServiceId(String networkServiceId) {
        this.networkServiceId = networkServiceId;
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }
    public String getVirtualNetworkFunctionId() {
        return virtualNetworkFunctionId;
    }

    public void setVirtualNetworkFunctionId(String virtualNetworkFunctionId) {
        this.virtualNetworkFunctionId = virtualNetworkFunctionId;
    }

    public Action getEvent() {
        return event;
    }

    public void setEvent(Action event) {
        this.event = event;
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
        return "EventEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", networkServiceId='" + networkServiceId + '\'' +
                ", virtualNetworkFunctionId='" + virtualNetworkFunctionId + '\'' +
                ", type=" + type +
                ", endpoint='" + endpoint + '\'' +
                ", event=" + event +
                '}';
    }

}
