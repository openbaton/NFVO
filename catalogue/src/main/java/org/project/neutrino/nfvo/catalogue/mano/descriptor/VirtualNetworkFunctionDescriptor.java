/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.descriptor;

import org.project.neutrino.nfvo.catalogue.mano.common.ConnectionPoint;
import org.project.neutrino.nfvo.catalogue.mano.common.NFVEntityDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.common.Security;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.TypeConstraintException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 05/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 */
@Entity
public class VirtualNetworkFunctionDescriptor extends NFVEntityDescriptor {
    /**
     * Version of the VNF Descriptor.
     * */
    private String descriptor_version;
    /**
     * This describes a set of elements related to a particular VDU
     * */
    @OneToMany(cascade=CascadeType.ALL)
    private List<VirtualDeploymentUnit> vdu;
    /**
     * Represents the type of network connectivity mandated by the VNF vendor between two or more Connection Point
     * */
    @OneToMany(cascade=CascadeType.ALL)
    private List<InternalVirtualLink> virtual_link;
    /**
     * Describe dependencies between VDUs. Defined in terms of source and target VDU, i.e. target VDU "depends on" source VDU. In other words sources VDU   shall exists before target VDU can be initiated/deployed.
     * */
    @OneToMany(cascade=CascadeType.ALL)
    private List<VDUDependency> vdu_dependency;

    /**
     * Represents the assurance parameter(s) and its requirement for each deployment flavour of the VNF being described, see clause 6.3.1.5.
     * */
    @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<VNFDDeploymentFlavour> deployment_flavour;
    /**
     * The VNF package may contain a file that lists all files in the package. This can be useful for auditing purposes or for enabling some security features on the package.
     * TODO consider having a stream of a pointer to a file
     * */
    private String manifest_file;
    /**
     * The manifest file may be created to contain a digest of each file that it lists as part of the package. This digest
     * information can form the basis of a security mechanism to ensure the contents of the package meet certain
     * security related properties. If the manifest file contains digests of the files in the package, then the manifest file should also note the
     * particular hash algorithm used to enable suitable verification mechanisms. Examples of suitable hash
     * algorithms include, but are not limited to SHA-256, SHA-384, SHA-512, and SHA-3. In conjunction with an appropriate security signing
     * mechanism, which may include having a security certificate as part of the VNF package, the digest
     * information can be used to help ensure the contents of the VNF package have not been tampered with.
     *
     * */
    @OneToMany(cascade=CascadeType.ALL)
    private List<Security> manifest_file_security;
	private String type;

	@Override
	public String toString() {
		return "VirtualNetworkFunctionDescriptor [descriptor_version="
				+ descriptor_version + ", vdu=" + vdu + ", virtual_link="
				+ virtual_link + ", vdu_dependency=" + vdu_dependency
				+ ", deployment_flavour=" + deployment_flavour
				+ ", manifest_file=" + manifest_file
				+ ", manifest_file_security=" + manifest_file_security
				+ ", id=" + id + ", hb_version=" + hb_version + ", vendor="
				+ vendor + ", version=" + version + ", vnffgd=" + vnffgd
				+ ", vld=" + vld + ", lifecycle_event=" + lifecycle_event
				+ ", monitoring_parameter=" + monitoring_parameter
				+ ", service_deployment_flavour=" + service_deployment_flavour
				+ ", auto_scale_policy=" + auto_scale_policy
				+ ", connection_point=" + connection_point + "]";
	}

	public VirtualNetworkFunctionDescriptor() {
    }

    public List<ConnectionPoint> getConnection_point() {
        return connection_point;
    }
    
    public List<VNFDConnectionPoint> getVNFDConnection_point() {
    	List<VNFDConnectionPoint> res = new ArrayList<VNFDConnectionPoint>();
    	for (ConnectionPoint cp : connection_point)
    		res.add((VNFDConnectionPoint)cp);
        return res;
    }

    public void setConnection_point(List<ConnectionPoint> connection_point) {
    	for (ConnectionPoint cp : connection_point)
    		if (!(cp instanceof VNFDConnectionPoint))
    				throw new TypeConstraintException("Connection Point " + cp.getId() + " field must be an instance of " + ConnectionPoint.class.getCanonicalName());
        this.connection_point = connection_point;
    }

	public String getDescriptor_version() {
		return descriptor_version;
	}

	public void setDescriptor_version(String descriptor_version) {
		this.descriptor_version = descriptor_version;
	}

	public List<VirtualDeploymentUnit> getVdu() {
		return vdu;
	}

	public void setVdu(List<VirtualDeploymentUnit> vdu) {
		this.vdu = vdu;
	}

	public List<InternalVirtualLink> getVirtual_link() {
		return virtual_link;
	}

	public void setVirtual_link(List<InternalVirtualLink> virtual_link) {
		this.virtual_link = virtual_link;
	}

	public List<VDUDependency> getVdu_dependency() {
		return vdu_dependency;
	}

	public void setVdu_dependency(List<VDUDependency> vdu_dependency) {
		this.vdu_dependency = vdu_dependency;
	}

	public List<VNFDDeploymentFlavour> getDeployment_flavour() {
		return deployment_flavour;
	}

	public void setDeployment_flavour(List<VNFDDeploymentFlavour> deployment_flavour) {
		this.deployment_flavour = deployment_flavour;
	}

	public String getManifest_file() {
		return manifest_file;
	}

	public void setManifest_file(String manifest_file) {
		this.manifest_file = manifest_file;
	}

	public List<Security> getManifest_file_security() {
		return manifest_file_security;
	}

	public void setManifest_file_security(List<Security> manifest_file_security) {
		this.manifest_file_security = manifest_file_security;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
