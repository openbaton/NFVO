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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 05/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 * The VLD describes the basic topology of the connectivity (e.g. E-LAN, E-Line, E-Tree) between one or more VNFs connected
 * to this VL and other required parameters (e.g. bandwidth and QoS class). The VLD connection parameters are expected to
 * have similar attributes to those used on the ports on VNFs in ETSI GS NFV-SWA 001 [i.8]. Therefore a set of VLs in a
 * Network Service can be mapped to a Network Connectivity Topology (NCT) as defined in ETSI GS NFV-SWA 001 [i.8].
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractVirtualLink implements Serializable{

    /**
     * ID of the VLD
     * */
	@Id
    protected String id;
	@Version
	protected int hb_version = 0;

    /**
     * extId of the network to attach
     */
    protected String extId;

        /**
     * Throughput of the link (e.g. bandwidth of E-Line, root bandwidth of E-Tree, and aggregate capacity of E-LAN)
     * */
    protected String root_requirement;
    /**
     * Throughput of leaf connections to the link (for E-Tree and E-LAN branches)
     * */
    protected String leaf_requirement;
    /**
     * QoS options available on the VL, e.g. latency, jitter, etc.
     * */
    @ElementCollection(fetch = FetchType.EAGER)
    protected Set<String> qos;
    /**
     * Test access facilities available on the VL (e.g. none, passive monitoring, or active (intrusive) loopbacks at endpoints
     * TODO think of using Enum instead of String
     * */
    @ElementCollection(fetch = FetchType.EAGER)
    protected Set<String> test_access;
    /**
     * Connectivity types, e.g. E-Line, E-LAN, or E-Tree.
     * TODO: think of using Enum instead of String
     * */
    protected String connectivity_type;
    /**
     * Name referenced by VNFCs
     */
    protected String name;

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }

    public AbstractVirtualLink() {
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public int getHb_version() {
	return hb_version;
    }

    public void setHb_version(int vb_version) {
        this.hb_version = hb_version;
    }


    public String getRoot_requirement() {
        return root_requirement;
    }

    public void setRoot_requirement(String root_requirement) {
        this.root_requirement = root_requirement;
    }

    public String getLeaf_requirement() {
        return leaf_requirement;
    }

    public void setLeaf_requirement(String leaf_requirement) {
        this.leaf_requirement = leaf_requirement;
    }

    public Set<String> getQos() {
        return qos;
    }

    public void setQos(Set<String> qos) {
        this.qos = qos;
    }

    public Set<String> getTest_access() {
        return test_access;
    }

    public void setTest_access(Set<String> test_access) {
        this.test_access = test_access;
    }

    public String getConnectivity_type() {
        return connectivity_type;
    }

    public void setConnectivity_type(String connectivity_type) {
        this.connectivity_type = connectivity_type;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
