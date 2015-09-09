/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

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
