/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.descriptor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

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
