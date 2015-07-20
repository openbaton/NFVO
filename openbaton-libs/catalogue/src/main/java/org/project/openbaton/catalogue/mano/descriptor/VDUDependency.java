/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VDUDependency implements Serializable{
	@Id
	private String id = IdGenerator.createUUID();
	@Version
	private int version = 0;
	
	@OneToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	private VirtualDeploymentUnit source;
	@OneToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	private VirtualDeploymentUnit target;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public VirtualDeploymentUnit getTarget() {
		return target;
	}

	public void setTarget(VirtualDeploymentUnit target) {
		this.target = target;
	}

	public VirtualDeploymentUnit getSource() {
		return source;
	}

	public void setSource(VirtualDeploymentUnit source) {
		this.source = source;
	}

	public VDUDependency() {

	}
}
