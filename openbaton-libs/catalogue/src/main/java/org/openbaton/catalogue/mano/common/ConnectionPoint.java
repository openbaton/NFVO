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

package org.openbaton.catalogue.mano.common;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 *
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ConnectionPoint implements Serializable{

    /*ID of the Connection Point.*/
    @Id
    protected String id;
    @Version
    protected int version = 0;

    /**
     *
     * This may be for example a virtual port, a virtual NIC address, a physical port, a physical NIC address or the
     * endpoint of an IP VPN enabling network connectivity.
     * TODO think about what type must be
     *
     * */
    protected String type;

    public ConnectionPoint() {
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }
    public String getType() {

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ConnectionPoint{" +
                "id='" + id + '\'' +
                ", hb_version='" + version +
                ", type='" + type + '\'' +
                '}';
    }

}
