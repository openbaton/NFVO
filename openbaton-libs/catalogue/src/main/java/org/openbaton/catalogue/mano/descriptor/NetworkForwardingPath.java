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

package org.openbaton.catalogue.mano.descriptor;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by lto on 06/02/15.
 * <p/>
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkForwardingPath implements Serializable {

    @Id
    private String id;
    @Version
    private int version = 0;
    /**
     * A policy or rule to apply to the NFP
     */
    @OneToOne(cascade = CascadeType.ALL)
    private Policy policy;
    /**
     * A tuple containing a reference to a Connection Point in the NFP and the position in the path
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, String> connection;

    public NetworkForwardingPath() {
    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Map<String, String> getConnection() {
        return connection;
    }

    public void setConnection(Map<String, String> connection) {
        this.connection = connection;
    }


    @Override
    public String toString() {
        return "NetworkForwardingPath{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", policy=" + policy +
                ", connection=" + connection +
                '}';
    }
}
