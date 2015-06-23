package org.project.neutrino.nfvo.core.cli.command;

import org.project.neutrino.nfvo.sdk.api.rest.Requestor;
import org.project.neutrino.nfvo.sdk.api.rest.NetworkServiceDescriptorRequest;
import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OpenBaton network-service-descriptor-related commands implementation using the spring-shell library.
 */
@Component
public class NetworkServiceDescriptor implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return networkServiceDescriptor: the Network Service Descriptor filled
	 *         with id and values from core
	 */
	@CliCommand(value = "networkServiceDescriptor create", help = "Submits and validates a new Network Service Descriptor (NSD)")
	public String create(
            @CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "NSD CREATED" + networkServiceDescriptorRequest.create(networkServiceDescriptor);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NSD NOT CREATED";
        }
	}

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	@CliCommand(value = "networkServiceDescriptor delete", help = "Removes a disabled Network Service Descriptor (NSD)")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            networkServiceDescriptorRequest.delete(id);
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
	@CliCommand(value = "networkServiceDescriptor find", help = "Returns the Network Service Descriptor (NSD) selected by id, or all if no id is given")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            if (id != null) {
                return "FOUND NSD: " + networkServiceDescriptorRequest.findById(id);
            } else {
                return "FOUND NSDs: " + networkServiceDescriptorRequest.findAll();
            }
        } catch (SDKException e) {
            log.debug(e.getMessage());
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
	@CliCommand(value = "networkServiceDescriptor update", help = "Updates he Network Service Descriptor (NSD)")
	public String update(
            @CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor,
            @CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "UPDATED NSD: " + networkServiceDescriptorRequest.update(networkServiceDescriptor, id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "NSD NOT UPDATED";
        }
	}

	/**
	 * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<VirtualNetworkFunctionDescriptor>: The List of
	 *         VirtualNetworkFunctionDescriptor into NSD
	 */
	@CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptors", help = "Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id")
	public String getVirtualNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND VNFDESCRIPTORs: " + networkServiceDescriptorRequest.getVirtualNetworkFunctionDescriptors(id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDESCRIPTOR NOT FOUND";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptor", help = "TODO")
	public String getVirtualNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "TODO") final String id_vfn) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND VNFDESCRIPTOR: " + networkServiceDescriptorRequest.getVirtualNetworkFunctionDescriptor(id, id_vfn);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDESCRIPTOR NOT FOUND";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor deleteVirtualNetworkFunctionDescriptor", help = "TODO")
	public String deleteVirtualNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "TODO") final String id_vfn) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            networkServiceDescriptorRequest.deleteVirtualNetworkFunctionDescriptors(id, id_vfn);
            return "DELETED VNFDESCRIPTOR";
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDESCRIPTOR NOT DELETED";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor postVNFD", help = "TODO")
	public String postVNFD(
			@CliOption(key = { "virtualNetworkFunctionDescriptorFile" }, mandatory = true, help = "TODO") final File virtualNetworkFunctionDescriptor,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "CREATED VNFDESCRIPTOR: " + networkServiceDescriptorRequest.postVNFD(virtualNetworkFunctionDescriptor, id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDESCRIPTOR NOT CREATED";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor updateVNF", help = "TODO")
	public String updateVNF(
			@CliOption(key = { "virtualNetworkFunctionDescriptorFile" }, mandatory = true, help = "TODO") final File virtualNetworkFunctionDescriptor,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vfn" }, mandatory = true, help = "TODO") final String id_vfn) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "UPDATED VNFDESCRIPTOR: " + networkServiceDescriptorRequest.updateVNF(virtualNetworkFunctionDescriptor, id, id_vfn);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDESCRIPTOR NOT UPDATED";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getVNFDependencies", help = "TODO")
	public String getVNFDependencies(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND VNFDEPENDENCIES: " + networkServiceDescriptorRequest.getVNFDependencies(id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDEPENDENCY NOT FOUND";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getVNFDependency", help = "TODO")
	public String getVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND VNFDEPENDENCY: " + networkServiceDescriptorRequest.getVNFDependency(id, id_vnfd);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDEPENDENCY NOT FOUND";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor deleteVNFDependency", help = "TODO")
	public String deleteVNFDependency(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            networkServiceDescriptorRequest.deleteVNFDependency(id, id_vnfd);
            return "DELETED VNFDEPENDENCY";
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDEPENDENCY NOT DELETED";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor postVNFDependency", help = "TODO")
	public String postVNFDependency(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "TODO") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "CREATED VNFDEPENDENCY: " + networkServiceDescriptorRequest.postVNFDependency(vnfDependency, id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDEPENDENCY NOT CREATED";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor updateVNFD", help = "TODO")
	public String updateVNFD(
			@CliOption(key = { "vnfDependencyFile" }, mandatory = true, help = "TODO") final File vnfDependency,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_vnfd" }, mandatory = true, help = "TODO") final String id_vnfd) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "UPDATED VNFDEPENDENCY: " + networkServiceDescriptorRequest.updateVNFD(vnfDependency, id, id_vnfd);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "VNFDEPENDENCY NOT UPDATED";
        }
	}

	/**
	 * Returns the list of PhysicalNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<PhysicalNetworkFunctionDescriptor>: The List of
	 *         PhysicalNetworkFunctionDescriptor into NSD
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptors", help = "TODO")
	public String getPhysicalNetworkFunctionDescriptors(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND PNFDESCRIPTORs: " + networkServiceDescriptorRequest.getPhysicalNetworkFunctionDescriptors(id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "PNFDESCRIPTOR NOT FOUND";
        }
	}

	/**
	 * Returns the PhysicalNetworkFunctionDescriptor
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor selected
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptor", help = "TODO")
	public String getPhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND PNFDESCRIPTOR: " + networkServiceDescriptorRequest.getPhysicalNetworkFunctionDescriptor(id, id_pnf);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "PNFDESCRIPTOR NOT FOUND";
        }
	}

	/**
	 * Deletes the PhysicalNetworkFunctionDescriptor with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 */
	@CliCommand(value = "networkServiceDescriptor deletePhysicalNetworkFunctionDescriptor", help = "TODO")
	public String deletePhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            networkServiceDescriptorRequest.deletePhysicalNetworkFunctionDescriptor(id, id_pnf);
            return "DELETED PNFDESCRIPTOR";
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "PNFDESCRIPTOR NOT DELETED";
        }
	}

	/**
	 * Stores the PhysicalNetworkFunctionDescriptor
	 *
	 * @param pnf
	 *            : The PhysicalNetworkFunctionDescriptor to be stored
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor stored
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor postPhysicalNetworkFunctionDescriptor", help = "TODO")
	public String postPhysicalNetworkFunctionDescriptor(
			@CliOption(key = { "pnfFile" }, mandatory = true, help = "TODO") final File pnf,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "CREATED PNFDESCRIPTOR: " + networkServiceDescriptorRequest.postPhysicalNetworkFunctionDescriptor(pnf, id, id_pnf);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "PNFDESCRIPTOR NOT CREATED";
        }
	}

	/**
	 * Edits the PhysicalNetworkFunctionDescriptor
	 *
	 * @param pnf
	 *            : The PhysicalNetworkFunctionDescriptor to be edited
	 * @param id
	 *            : The NSD id
	 * @return PhysicalNetworkFunctionDescriptor: The
	 *         PhysicalNetworkFunctionDescriptor edited
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor updatePNFD", help = "TODO")
	public String updatePNFD(
			@CliOption(key = { "pnfFile" }, mandatory = true, help = "TODO") final File pnf,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_pnf" }, mandatory = true, help = "TODO") final String id_pnf) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "UPDATED PNFDESCRIPTOR: " + networkServiceDescriptorRequest.updatePNFD(pnf, id, id_pnf);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "PNFDESCRIPTOR NOT UPDATED";
        }
	}

	/**
	 * Returns the Security into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security of PhysicalNetworkFunctionDescriptor into
	 *         NSD
	 */
	@CliCommand(value = "networkServiceDescriptor getSecurities", help = "TODO")
	public String getSecurities(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND SECURITIES: " + networkServiceDescriptorRequest.getSecurities(id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "SECURITY NOT FOUND";
        }
	}

	/**
	 * Returns the Security with the id_s
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_s
	 *            : The Security id
	 * @return Security: The Security selected by id_s
	 */
	@CliCommand(value = "networkServiceDescriptor getSecurity", help = "TODO")
	public String getSecurity(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "TODO") final String id_s) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "FOUND SECURITY: " + networkServiceDescriptorRequest.getSecurity(id, id_s);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "SECURITY NOT FOUND";
        }
	}

	/**
	 * Deletes the Security with the id_s
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_s
	 *            : The Security id
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor deleteSecurity", help = "TODO")
	public String deleteSecurity(
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "TODO") final String id_s) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            networkServiceDescriptorRequest.deleteSecurity(id, id_s);
            return "DELETED SECURITY";
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "SECURITY NOT DELETED";
        }
	}

	/**
	 * Stores the Security into NSD
	 *
	 * @param security
	 *            : The Security to be stored
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security stored
	 * @
	 */
	@CliCommand(value = "networkServiceDescriptor postSecurity", help = "TODO")
	public String postSecurity(
			@CliOption(key = { "securityFile" }, mandatory = true, help = "TODO") final File security,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "CREATED SECURITY: " + networkServiceDescriptorRequest.postSecurity(security, id);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "SECURITY NOT CREATED";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor updateSecurity", help = "TODO")
	public String updateSecurity(
			@CliOption(key = { "securityFile" }, mandatory = true, help = "TODO") final File security,
			@CliOption(key = { "id" }, mandatory = true, help = "The networkServiceDescriptor id") final String id,
			@CliOption(key = { "id_s" }, mandatory = true, help = "TODO") final String id_s) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "UPDATED SECURITY: " + networkServiceDescriptorRequest.updateSecurity(security, id, id_s);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "SECURITY NOT UPDATED";
        }
	}

	/**
	 *
	 */
	@CliCommand(value = "networkServiceDescriptor createRecord", help = "TODO")
	public String createRecord(
			@CliOption(key = { "networkServiceDescriptorFile" }, mandatory = true, help = "TODO") final File networkServiceDescriptor) {
        try {
            NetworkServiceDescriptorRequest networkServiceDescriptorRequest = Requestor.getNetworkServiceDescriptorRequest();
            return "CREATED RECORD: " + networkServiceDescriptorRequest.createRecord(networkServiceDescriptor);
        } catch (SDKException e) {
            log.debug(e.getMessage());
            return "RECORD NOT CREATED";
        }
	}
}
