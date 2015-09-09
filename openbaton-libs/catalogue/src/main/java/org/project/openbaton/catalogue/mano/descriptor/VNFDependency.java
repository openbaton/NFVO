package org.project.openbaton.catalogue.mano.descriptor;

/**
 * Created by lto on 05/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Describe dependencies between VNF. Defined in terms of
 * source and target VNF i.e. target VNF "depends on" source
 * VNF. In other words a source VNF shall exist and connect to
 * the service before target VNF can be initiated/deployed and
 * connected. This element would be used, for example, to define
 * the sequence in which various numbered network nodes and
 * links within a VNF FG should be instantiated by the NFV
 * Orchestrator.*/
@Entity
public class VNFDependency implements Serializable {

	@Id
	private String id;
	@Version
	private int version = 0;

	@ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false)
    private VirtualNetworkFunctionDescriptor source;

	@ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false)
	private VirtualNetworkFunctionDescriptor target;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> parameters;

    public VNFDependency() {
    }

	@PrePersist
	public void ensureId(){
		id=IdGenerator.createUUID();
	}
    public VirtualNetworkFunctionDescriptor getSource() {
        return source;
    }

    public void setSource(VirtualNetworkFunctionDescriptor source) {
        this.source = source;
    }

	public Set<String> getParameters() {
		return parameters;
	}

	public void setParameters(Set<String> parameters) {
		this.parameters = parameters;
	}

    public VirtualNetworkFunctionDescriptor getTarget() {
        return target;
    }

    public void setTarget(VirtualNetworkFunctionDescriptor target) {
        this.target = target;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "VNFDependency [id=" + id + ", version=" + version + ", source="
				+ (source == null ? source : source.getName()) + ", target=" + (target == null ? target : target.getName()) +
				", parameters=" + parameters +
				"]";
	}


}
