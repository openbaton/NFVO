package org.project.openbaton.nfvo.core.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.sdk.NFVORequestor;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.project.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * OpenBaton network-service-descriptor-related commands implementation using the spring-shell library.
 */
@Component
public class NetworkServiceDescriptor implements CommandMarker {

	private Logger log = LoggerFactory.getLogger(this.getClass());

    private NFVORequestor requestor = new NFVORequestor("1");
    private AbstractRestAgent<org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor> networkServiceDescriptorAgent;
    private Gson mapper = new Gson();

    @PostConstruct
    private void init(){
        networkServiceDescriptorAgent = requestor.getNetworkServiceDescriptorAgent();
    }
    private org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor getObject(File networkServiceDescriptor) throws FileNotFoundException {
        return mapper.<org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor>fromJson(new InputStreamReader(new FileInputStream(networkServiceDescriptor)), org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor.class);
    }

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return networkServiceDescriptor: the Network Service Descriptor filled
	 *         with id and values from core
	 */
	@CliCommand(value = "networkServiceDescriptor create", help = "Submit and validate a new Network Service Descriptor (NSD)")
	public String create(
            @CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor) {
        try {
            return "NSD CREATED" + networkServiceDescriptorAgent.create(getObject(networkServiceDescriptor));
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NSD NOT CREATED";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "NSD NOT CREATED";
        }
    }

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	@CliCommand(value = "networkServiceDescriptor delete", help = "Remove a disabled Network Service Descriptor (NSD)")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            networkServiceDescriptorAgent.delete(id);
            return "NETWORKSERVICEDESCRIPTOR DELETED";
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NETWORKSERVICEDESCRIPTOR NOT DELETED";
        }
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceDescriptor: the Network Service Descriptor selected
	 */
	@CliCommand(value = "networkServiceDescriptor find", help = "Return the Network Service Descriptor (NSD) selected by id, or all if no id is given")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            if (id != null) {
                return "FOUND NSD: " + networkServiceDescriptorAgent.findById(id);
            } else {
                return "FOUND NSDs: " + networkServiceDescriptorAgent.findAll();
            }
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NO NSD FOUND";
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return "NO NSD FOUND";
        }
    }

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be updated
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return networkServiceDescriptor: the Network Service Descriptor updated
	 */
	@CliCommand(value = "networkServiceDescriptor update", help = "Update he Network Service Descriptor (NSD)")
	public String update(
            @CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor,
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            return "UPDATED NSD: " + networkServiceDescriptorAgent.update(getObject(networkServiceDescriptor), id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NSD NOT UPDATED";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "NSD NOT UPDATED";
        }
    }

	/**
	 * Return the list of VirtualNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<VirtualNetworkFunctionDescriptor>: The List of
	 *         VirtualNetworkFunctionDescriptor into NSD
	 */
	@CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptors", help = "Return the list of VirtualNetworkFunctionDescriptor into a NSD with id")
	public String getVirtualNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND VNFDESCRIPTORs: " + networkServiceDescriptorAgent.getVirtualNetworkFunctionDescriptors(id);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDESCRIPTOR NOT FOUND";
//        }
	}

    /**
     * Return a VirtualNetworkFunctionDescriptor into a NSD with id
     *
     * @param id
     *            : The id of NSD
     * @param id_vfn
     *            : The id of the VNF Descriptor
     * @return List<VirtualNetworkFunctionDescriptor>: The List of
     *         VirtualNetworkFunctionDescriptor into NSD
     */
	@CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptor", help = "Return the list of VirtualNetworkFunctionDescriptor into a NSD with id")
	public String getVirtualNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "The virtual network function descriptor id") final String id_vfn) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND VNFDESCRIPTOR: " + networkServiceDescriptorRequest.getVirtualNetworkFunctionDescriptor(id, id_vfn);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDESCRIPTOR NOT FOUND";
//        }
	}

    /**
     * Delete the VirtualNetworkFunctionDescriptor
     *
     * @param id
     *            : The id of NSD
     * @param id_vfn
     *            : The id of the VNF Descriptor
     */
	@CliCommand(value = "networkServiceDescriptor deleteVirtualNetworkFunctionDescriptor", help = "Delete the VirtualNetworkFunctionDescriptor")
	public String deleteVirtualNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "The virtual network function descriptor id") final String id_vfn) {
        throw new UnsupportedOperationException();
//        try {
//            networkServiceDescriptorRequest.deleteVirtualNetworkFunctionDescriptors(id, id_vfn);
//            return "DELETED VNFDESCRIPTOR";
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDESCRIPTOR NOT DELETED";
//        }
	}

    /**
     * Create a VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor
     *            : : the Network Service Descriptor to be updated
     * @param id
     *            : The id of the networkServiceDescriptor the vnfd shall be created at
     */
	@CliCommand(value = "networkServiceDescriptor postVNFD", help = "Create a VirtualNetworkFunctionDescriptor")
	public String postVNFD(
			@CliOption(key = { "virtualNetworkFunctionDescriptorFile" }, mandatory = true, help = "The virtualNetworkFunction json file") final File virtualNetworkFunctionDescriptor,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        throw new UnsupportedOperationException();
//        try {
//            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
//            return "CREATED VNFDESCRIPTOR: " + networkServiceDescriptorRequest.postVNFD(virtualNetworkFunctionDescriptor, id);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDESCRIPTOR NOT CREATED";
//        }
	}

    /**
     * Update the VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor
     *            : : the Network Service Descriptor to be updated
     * @param id
     *            : The id of the (old) VNF Descriptor
     * @param id_vfn
     *            : The id of the VNF Descriptor
     * @return List<VirtualNetworkFunctionDescriptor>: The updated virtualNetworkFunctionDescriptor
     */
	@CliCommand(value = "networkServiceDescriptor updateVNF", help = "update the VirtualNetworkFunctionDescriptor")
	public String updateVNF(
			@CliOption(key = { "virtualNetworkFunctionDescriptorFile" }, mandatory = true, help = "The virtualNetworkFunction json file") final File virtualNetworkFunctionDescriptor,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "The virtual network function descriptor id") final String id_vfn) {
        throw new UnsupportedOperationException();
//        try {
//            return "UPDATED VNFDESCRIPTOR: " + networkServiceDescriptorRequest.updateVNF(virtualNetworkFunctionDescriptor, id, id_vfn);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDESCRIPTOR NOT UPDATED";
//        }
	}

    /**
     * Return the list of VNFDependencies into a NSD
     *
     * @param id
     *            : The id of the networkServiceDescriptor
     * @return List<VNFDependency>:  The List of VNFDependency into NSD
     */
	@CliCommand(value = "networkServiceDescriptor getVNFDependencies", help = "Return the list of VNFDependencies into a NSD")
	public String getVNFDependencies(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND VNFDEPENDENCIES: " + networkServiceDescriptorRequest.getVNFDependencies(id);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDEPENDENCY NOT FOUND";
//        }
	}

    /**
     * Return a VNFDependency into a NSD
     *
     * @param id
     *            : The id of the VNF Descriptor
     * @param id_vnfd
     *            : The VNFDependencies id
     * @return VNFDependency:  The List of VNFDependency into NSD
     */
	@CliCommand(value = "networkServiceDescriptor getVNFDependency", help = "Return a VNFDependency into a NSD")
	public String getVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "The VNFDependencies id") final String id_vnfd) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND VNFDEPENDENCY: " + networkServiceDescriptorRequest.getVNFDependency(id, id_vnfd);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDEPENDENCY NOT FOUND";
//        }
	}

    /**
     * Delets a VNFDependency
     *
     * @param id
     *            : The id of the networkServiceDescriptor
     * @param id_vnfd
     *            : The id of the VNFDependency
     */
	@CliCommand(value = "networkServiceDescriptor deleteVNFDependency", help = "Delete a VNFDependency")
	public String deleteVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "The VNFDependencies id") final String id_vnfd) {
        throw new UnsupportedOperationException();
//        try {
//            networkServiceDescriptorRequest.deleteVNFDependency(id, id_vnfd);
//            return "DELETED VNFDEPENDENCY";
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDEPENDENCY NOT DELETED";
//        }
	}

    /**
     * Create a VNFDependency
     *
     * @param vnfDependency
     *            : The VNFDependency to be updated
     * @param id
     *            : The id of the networkServiceDescriptor
     */
	@CliCommand(value = "networkServiceDescriptor postVNFDependency", help = "Creates a VNFDependency")
	public String postVNFDependency(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "The VNFDependency json file") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        throw new UnsupportedOperationException();
//        try {
//            return "CREATED VNFDEPENDENCY: " + networkServiceDescriptorRequest.postVNFDependency(vnfDependency, id);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDEPENDENCY NOT CREATED";
//        }
	}

    /**
     * Update the VNFDependency
     *
     * @param vnfDependency
     *            : The VNFDependency to be updated
     * @param id
     *            : The id of the networkServiceDescriptor
     * @param id_vnfd
     *            : The id of the VNFDependency
     * @return The updated VNFDependency
     */
	@CliCommand(value = "networkServiceDescriptor updateVNFD", help = "Update the VNFDependency")
	public String updateVNFD(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "Update the VNFDependency") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "The VNFDependencies id") final String id_vnfd) {
        throw new UnsupportedOperationException();
//        try {
//            return "UPDATED VNFDEPENDENCY: " + networkServiceDescriptorRequest.updateVNFD(vnfDependency, id, id_vnfd);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "VNFDEPENDENCY NOT UPDATED";
//        }
	}

	/**
	 * Return the list of PhysicalNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<PhysicalNetworkFunctionDescriptor>: The List of
	 *         PhysicalNetworkFunctionDescriptor into NSD
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptors", help = "Return the list of PhysicalNetworkFunctionDescriptor into a NSD with id")
	public String getPhysicalNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND PNFDESCRIPTORs: " + networkServiceDescriptorRequest.getPhysicalNetworkFunctionDescriptors(id);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "PNFDESCRIPTOR NOT FOUND";
//        }
	}

	/**
	 * Returns the PhysicalNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor selected
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptor", help = "Return the PhysicalNetworkFunctionDescriptor into a NSD with id")
	public String getPhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor id") final String id_pnf) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND PNFDESCRIPTOR: " + networkServiceDescriptorRequest.getPhysicalNetworkFunctionDescriptor(id, id_pnf);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "PNFDESCRIPTOR NOT FOUND";
//        }
	}

	/**
	 * Delete the PhysicalNetworkFunctionDescriptor with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 */
	@CliCommand(value = "networkServiceDescriptor deletePhysicalNetworkFunctionDescriptor", help = "Delete the PhysicalNetworkFunctionDescriptor with the id_pnf")
	public String deletePhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor id") final String id_pnf) {
        throw new UnsupportedOperationException();
//        try {
//            networkServiceDescriptorRequest.deletePhysicalNetworkFunctionDescriptor(id, id_pnf);
//            return "DELETED PNFDESCRIPTOR";
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "PNFDESCRIPTOR NOT DELETED";
//        }
	}

	/**
	 * Store the PhysicalNetworkFunctionDescriptor
	 *
	 * @param pnf
	 *            : The PhysicalNetworkFunctionDescriptor to be stored
	 * @param id
	 *            : The NSD id
     * @param id_pnf
     *            : The PhysicalNetworkFunctionDescriptor id
	 * @return PhysicalNetworkFunctionDescriptor: The PhysicalNetworkFunctionDescriptor stored
	 */
	@CliCommand(value = "networkServiceDescriptor postPhysicalNetworkFunctionDescriptor", help = "Store the PhysicalNetworkFunctionDescriptor")
	public String postPhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "pnfFile" }, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor json file") final File pnf,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor id") final String id_pnf) {
        throw new UnsupportedOperationException();
//        try {
//            return "CREATED PNFDESCRIPTOR: " + networkServiceDescriptorRequest.postPhysicalNetworkFunctionDescriptor(pnf, id, id_pnf);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "PNFDESCRIPTOR NOT CREATED";
//        }
	}

	/**
	 * Update the PhysicalNetworkFunctionDescriptor
	 *
	 * @param pnf
	 *            : The PhysicalNetworkFunctionDescriptor to be edited
	 * @param id
     *            : The NSD id
     * @param id_pnf
     *            : The PhysicalNetworkFunctionDescriptor id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor edited
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor updatePNFD", help = "Update the PhysicalNetworkFunctionDescriptor")
	public String updatePNFD(
			@CliOption(key = { "pnfFile" }, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor json file") final File pnf,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor id") final String id_pnf) {
        throw new UnsupportedOperationException();
//        try {
//            return "UPDATED PNFDESCRIPTOR: " + networkServiceDescriptorRequest.updatePNFD(pnf, id, id_pnf);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "PNFDESCRIPTOR NOT UPDATED";
//        }
	}

	/**
	 * Return the Security into a NSD
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security of PhysicalNetworkFunctionDescriptor into
	 *         NSD
	 */
	@CliCommand(value = "networkServiceDescriptor getSecurities", help = "Return all Security from a nsd")
	public String getSecurities(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND SECURITIES: " + networkServiceDescriptorRequest.getSecurities(id);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "SECURITY NOT FOUND";
//        }
	}

	/**
	 * Return the Security with the id_s
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_s
	 *            : The Security id
	 * @return Security: The Security selected by id_s
	 */
	@CliCommand(value = "networkServiceDescriptor getSecurity", help = "Return the Security with the id_s")
	public String getSecurity(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "The security id") final String id_s) {
        throw new UnsupportedOperationException();
//        try {
//            return "FOUND SECURITY: " + networkServiceDescriptorRequest.getSecurity(id, id_s);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "SECURITY NOT FOUND";
//        }
	}

	/**
	 * Delete the Security with the id_s
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_s
	 *            : The Security id
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor deleteSecurity", help = "Delete the Security with the id_s")
	public String deleteSecurity(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "The security id") final String id_s) {
        throw new UnsupportedOperationException();
//        try {
//            networkServiceDescriptorRequest.deleteSecurity(id, id_s);
//            return "DELETED SECURITY";
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "SECURITY NOT DELETED";
//        }
	}

	/**
	 * Store the Security into NSD
	 *
	 * @param security
	 *            : The Security to be stored
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security stored
	 */
	@CliCommand(value = "networkServiceDescriptor postSecurity", help = " Store the Security into NSD")
	public String postSecurity(
			@CliOption(key = { "securityFile" }, mandatory = true, help = "The Security json file") final File security,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        throw new UnsupportedOperationException();
//        try {
//            return "CREATED SECURITY: " + networkServiceDescriptorRequest.postSecurity(security, id);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "SECURITY NOT CREATED";
//        }
	}

    /**
     * Update the Security into NSD
     *
     * @param security
     *            : The Security to be stored
     * @param id
     *            : The id of NSD
     * @param id_s
     *            : The security id
     * @return Security: The Security stored
     */
	@CliCommand(value = "networkServiceDescriptor updateSecurity", help = "Update the Security into NSD")
	public String updateSecurity(
			@CliOption(key = { "securityFile" }, mandatory = true, help = "The Security json file") final File security,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "The security id") final String id_s) {
        throw new UnsupportedOperationException();
//        try {
//            return "UPDATED SECURITY: " + networkServiceDescriptorRequest.updateSecurity(security, id, id_s);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "SECURITY NOT UPDATED";
//        }
	}

    /**
     * Create a record into NSD
     *
     * @param networkServiceDescriptor
     *            : the networkServiceDescriptor JSON File
     */
	@CliCommand(value = "networkServiceDescriptor createRecord", help = "Create a record into NSD")
	public String createRecord(
			@CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor) {
        throw new UnsupportedOperationException();
//        try {
//            return "CREATED RECORD: " + networkServiceDescriptorRequest.createRecord(networkServiceDescriptor);
//        } catch (SDKException e) {
//            log.debug(e.getMessage());
//            return "RECORD NOT CREATED";
//        }
	}
}
