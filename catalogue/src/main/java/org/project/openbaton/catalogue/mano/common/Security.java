package org.project.openbaton.catalogue.mano.common;

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
 */
@Entity
public class Security implements Serializable {
	@Id
	private String id;
	@Version
	private int version = 0;

	public String getId() {
		return id;
	}
	@PrePersist
	public void ensureId(){
		id=IdGenerator.createUUID();
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Security [id=" + id + ", version=" + version + "]";
	}
	
}
