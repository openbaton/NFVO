package de.fhg.fokus.ngni.nfvo.repository.mano.common;

import de.fhg.fokus.ngni.nfvo.repository.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class Security implements Serializable{
	@Id
	private String id = IdGenerator.createUUID();
	@Version
	private int version = 0;
}
