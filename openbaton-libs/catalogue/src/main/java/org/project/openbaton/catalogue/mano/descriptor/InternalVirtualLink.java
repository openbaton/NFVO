/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.mano.common.AbstractVirtualLink;

import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class InternalVirtualLink extends AbstractVirtualLink {

    /**
     * References to Connection Points (vnfd:vdu:vnfc:connection_point:id,vnfd:connection_point:id), e.g. of type E-Line, E-Tree, or E-LAN.
     * */
	@ElementCollection(fetch = FetchType.EAGER)
    private Set<String> connection_points_references;

    public InternalVirtualLink() {
    }

    public Set<String> getConnection_points_references() {
        return connection_points_references;
    }

    public void setConnection_points_references(Set<String> connection_points_references) {
        this.connection_points_references = connection_points_references;
    }

    @Override
    public String toString() {
        return "InternalVirtualLink{" +
                "id='" + id + '\'' +
                ", version='" + version +
                ", connectivity_type='" + getConnectivity_type() + '\'' +
                ", connection_points_references='" + connection_points_references + '\'' +
                ", root_requirement='" + getRoot_requirement() + '\'' +
                ", leaf_requirement='" + getLeaf_requirement() + '\'' +
                ", qos='" + getQos() + '\'' +
                ", test_access='" + getTest_access() + '\'' +
                '}';
    }

}
