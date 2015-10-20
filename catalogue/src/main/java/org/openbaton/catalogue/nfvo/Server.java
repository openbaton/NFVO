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

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.util.IdGenerator;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lto on 20/05/15.
 */
//@Entity
public class Server implements Serializable {
//    @Id
    private String id;
//    @Version
    private int version = 0;
    private String name;

//    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private NFVImage image;
//    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private DeploymentFlavour flavor;

    private String status;
    private String extendedStatus;
    private String extId;

//    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, List<String>> ips;

//    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, String> floatingIps;

//    @Temporal(TemporalType.DATE)
    private Date created;
//    @Temporal(TemporalType.DATE)
    private Date updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

//    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Map<String, List<String>> getIps() {
        return ips;
    }

    public void setIps(Map<String, List<String>> ips) {
        this.ips = ips;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getExtendedStatus() {
        return extendedStatus;
    }

    public void setExtendedStatus(String extendedStatus) {
        this.extendedStatus = extendedStatus;
    }

    public NFVImage getImage() {
        return image;
    }

    public void setImage(NFVImage image) {
        this.image = image;
    }

    public DeploymentFlavour getFlavor() {
        return flavor;
    }

    public void setFlavor(DeploymentFlavour flavor) {
        this.flavor = flavor;
    }

    public Map<String, String> getFloatingIps() {
        return floatingIps;
    }

    public void setFloatingIps(Map<String, String> floatingIps) {
        this.floatingIps = floatingIps;
    }

    @Override
    public String toString() {
        return "Server{" +
                "created=" + created +
                ", id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", image=" + image +
                ", flavor=" + flavor +
                ", status='" + status + '\'' +
                ", extendedStatus='" + extendedStatus + '\'' +
                ", extId='" + extId + '\'' +
                ", ips=" + ips +
                ", floatingIps=" + floatingIps +
                ", updated=" + updated +
                '}';
    }
}

