/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

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
 *
 * A policy or rule to apply to the NFP
 */
@Entity
public class Policy implements Serializable{
	
	@Id
	private String id;
	@Version
	private int version = 0;

    public Policy(){}

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }

}
