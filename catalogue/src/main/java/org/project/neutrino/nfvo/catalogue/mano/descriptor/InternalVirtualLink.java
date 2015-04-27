/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.descriptor;

import org.project.neutrino.nfvo.catalogue.mano.common.AbstractVirtualLink;

import java.util.List;

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
    private List<String> connection_points_references;

    public InternalVirtualLink() {
    }

    public List<String> getConnection_points_references() {
        return connection_points_references;
    }

    public void setConnection_points_references(List<String> connection_points_references) {
        this.connection_points_references = connection_points_references;
    }
}
