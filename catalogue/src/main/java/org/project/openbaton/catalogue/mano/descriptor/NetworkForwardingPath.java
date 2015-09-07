/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkForwardingPath implements Serializable{

	@Id
    private String id;
	@Version
	private int version = 0;
    /**
     * A policy or rule to apply to the NFP
     * */
	@OneToOne(cascade = CascadeType.ALL)
    private Policy policy;
    /**
     * A tuple containing a reference to a Connection Point in the NFP and the position in the path
     * */
	@ElementCollection(fetch = FetchType.EAGER)
    private Map<String, String> connection;

    public NetworkForwardingPath() {
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

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Map<String, String> getConnection() {
        return connection;
    }

    public void setConnection(Map<String, String> connection) {
        this.connection = connection;
    }
}
