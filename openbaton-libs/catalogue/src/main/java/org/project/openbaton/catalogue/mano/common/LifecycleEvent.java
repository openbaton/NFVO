/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.common;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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

	@Enumerated(EnumType.STRING)
	private Event event;

	@ElementCollection(fetch = FetchType.EAGER)
    private Set<String> lifecycle_events;

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


	@Override
	public String toString() {
		return "LifecycleEvent{" +
				"id='" + id + '\'' +
				", version=" + version +
				", event=" + event +
				", lifecycle_events=" + lifecycle_events +
				'}';
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Set<String> getLifecycle_events() {
		return lifecycle_events;
	}

	public void setLifecycle_events(Set<String> lifecycle_events) {
		this.lifecycle_events = lifecycle_events;
	}
}
