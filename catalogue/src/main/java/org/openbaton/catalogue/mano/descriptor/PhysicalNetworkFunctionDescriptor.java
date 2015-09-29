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

import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 */
@Entity
public class PhysicalNetworkFunctionDescriptor implements Serializable{

    /**
     * The ID (e.g. name) of this PNFD.
     * */
    @Id
	private String id;
    @Version
    private int hb_version = 0;
    /**
     *The vendor generating this PNFD.
     * */
    private String vendor;
    /**

     * The version of PNF this PNFD is describing.
     * */
    private String version;
    /**
     * Description of physical device functionality.
     * */
    private String description;
    /**
     * This element describes an external interface exposed by this PNF enabling connection with a VL.
     * */
    @OneToMany(cascade = CascadeType.ALL)
    private Set<ConnectionPoint> connection_point;
    /**
     * Version of the PNF descriptor.
     * */
    private String descriptor_version;
    /**
     * This is a signature of pnfd to prevent tampering. The particular hash algorithm used to compute the signature,
     * together with the corresponding cryptographic certificate to validate the signature should also be included.
     * */
    @OneToOne(cascade = CascadeType.ALL)
    private Security pnfd_security;
    public PhysicalNetworkFunctionDescriptor() {

    }

    public int getHb_version() {
        return hb_version;
    }

    public void setHb_version(int hb_version) {
        this.hb_version = hb_version;
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ConnectionPoint> getConnection_point() {
        return connection_point;
    }

    public void setConnection_point(Set<ConnectionPoint> connection_point) {
        this.connection_point = connection_point;
    }

    public String getDescriptor_version() {
        return descriptor_version;
    }

    public void setDescriptor_version(String descriptor_version) {
        this.descriptor_version = descriptor_version;
    }

    public Security getPnfd_security() {
        return pnfd_security;
    }

    public void setPnfd_security(Security pnfd_security) {
        this.pnfd_security = pnfd_security;
    }

	@Override
	public String toString() {
		return "PhysicalNetworkFunctionDescriptor [id=" + id + ", hb_version="
				+ hb_version + ", vendor=" + vendor + ", version=" + version
				+ ", description=" + description + ", connection_point="
				+ connection_point + ", descriptor_version="
				+ descriptor_version + ", pnfd_security=" + pnfd_security + "]";
	}
}
