/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package de.fhg.fokus.ngni.nfvo.repository.mano.descriptor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import de.fhg.fokus.ngni.nfvo.repository.util.IdGenerator;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 * A policy or rule to apply to the NFP
 */
@Entity
public class Policy {
	
	@Id
	private String id = IdGenerator.createUUID();
	@Version
	private int version = 0;

}
