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

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 * <p/>
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VDUDependency implements Serializable {
    @Id
    private String id;
    @Version
    private int version = 0;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private VirtualDeploymentUnit source;
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private VirtualDeploymentUnit target;

    public VDUDependency() {

    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
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

    public VirtualDeploymentUnit getTarget() {
        return target;
    }

    public void setTarget(VirtualDeploymentUnit target) {
        this.target = target;
    }

    public VirtualDeploymentUnit getSource() {
        return source;
    }

    public void setSource(VirtualDeploymentUnit source) {
        this.source = source;
    }
}
