/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.common;

import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class LifecycleEvent implements Serializable{

	@Id
	private String id = IdGenerator.createUUID();
	
	@Version
	private int version = 0;
	
	@ElementCollection(fetch = FetchType.EAGER)
    private Map<Event,String> lifecycle_events;

    public LifecycleEvent() {
    }

    
    
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



	public Map<Event, String> getLifecycle_events() {
        return lifecycle_events;
    }

    public void setLifecycle_events(Map<Event, String> lifecycle_events) {
        this.lifecycle_events = lifecycle_events;
    }
}
