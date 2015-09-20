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

import org.project.openbaton.catalogue.mano.common.ConnectionPoint;

import javax.persistence.Entity;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VNFDConnectionPoint extends ConnectionPoint {
    /**
     * References an internal Virtual Link (vnfd:virtual_link:id, see clause 6.3.1.3) to which other VDUs, NFs, and other types of endpoints can connect.
     * */
    private String virtual_link_reference;

    public VNFDConnectionPoint() {
    }

    public String getVirtual_link_reference() {
        return virtual_link_reference;
    }

    public void setVirtual_link_reference(String virtual_link_reference) {
        this.virtual_link_reference = virtual_link_reference;
    }
}
