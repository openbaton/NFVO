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

package org.project.openbaton.catalogue.mano.common;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class CostituentVDU implements Serializable{
    @Id
    private String id;
    @Version
    private int version = 0;
    /**
     * References a VDU which should be used for this deployment flavour by vnfd:vdu:id, see clause 6.3.1.2.1.
     * */

    private String vdu_reference;
    /**
     * Number of VDU instances required
     * */
    private int number_of_instances;
    /**
     * References VNFCs which should be used for this deployment flavour by vnfd:vdu:vnfc:id
     * TODO understand what is a VNF component
     * */
    private String constituent_vnfc;
    public CostituentVDU() {

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

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }

    public String getVdu_reference() {
        return vdu_reference;
    }

    public void setVdu_reference(String vdu_reference) {
        this.vdu_reference = vdu_reference;
    }

    public int getNumber_of_instances() {
        return number_of_instances;
    }

    public void setNumber_of_instances(int number_of_instances) {
        this.number_of_instances = number_of_instances;
    }

    public String getConstituent_vnfc() {
        return constituent_vnfc;
    }

    public void setConstituent_vnfc(String constituent_vnfc) {
        this.constituent_vnfc = constituent_vnfc;
    }
}
