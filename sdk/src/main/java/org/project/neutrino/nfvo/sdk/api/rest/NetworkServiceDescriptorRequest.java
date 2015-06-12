package org.project.neutrino.nfvo.sdk.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OpenBaton network-service-descriptor-related api requester.
 */
@Component
public class NetworkServiceDescriptorRequest {
	
//	private static Logger log = LoggerFactory.getLogger("SDKApi");
	
	@Autowired
	private ConfigurableApplicationContext context;

	/**
	 * This operation allows submitting and validating a Network Service
	 * Descriptor (NSD), including any related VNFFGD and VLD.
	 *
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be created
	 * @return networkServiceDescriptor: the Network Service Descriptor filled
	 *         with id and values from core
	 */
	public String create(final File networkServiceDescriptor) {
		return "IMAGE CREATED";
	}

	/**
	 * This operation is used to remove a disabled Network Service Descriptor
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 */
	public String delete(final String id) {
		return "IMAGE CREATED";
	}

	/**
	 * This operation returns the list of Network Service Descriptor (NSD)
	 *
	 * @return List<NetworkServiceDescriptor>: the list of Network Service
	 *         Descriptor stored
	 */
	public String findAll() {
		return "IMAGE RESULTS";
	}

	/**
	 * This operation returns the Network Service Descriptor (NSD) selected by
	 * id
	 *
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return NetworkServiceDescriptor: the Network Service Descriptor selected
	 */
	public String findById(final String id) {
		return "IMAGE RESULT";
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
	public String update(final File networkServiceDescriptor, final String id) {
		return "IMAGE UPDATED";
	}

	/////////////////////////////////////////////////////////////////

	/**
	 * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return List<VirtualNetworkFunctionDescriptor>: The List of
	 *         VirtualNetworkFunctionDescriptor into NSD
	 */
	public String getVirtualNetworkFunctionDescriptors(final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionDescriptor(final String id, final String id_vfn) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVirtualNetworkFunctionDescriptors(final String id, final String id_vfn) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String postVNFD(final File virtualNetworkFunctionDescriptor, final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String updateVNF(final File virtualNetworkFunctionDescriptor, final String id, final String id_vfn) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVNFDependencies(final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String getVNFDependency(final String id, final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String deleteVNFDependency(final String id, final String id_vnfd) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String postVNFDependency(final File vnfDependency, final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String updateVNFD(final File vnfDependency, final String id, final String id_vnfd) {
		return "IMAGE UPDATED";
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
	public String getPhysicalNetworkFunctionDescriptors(final String id) {
		return "IMAGE UPDATED";
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
	public String getPhysicalNetworkFunctionDescriptor(final String id, final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Deletes the PhysicalNetworkFunctionDescriptor with the id_pnf
	 *
	 * @param id
	 *            : The NSD id
	 * @param id_pnf
	 *            : The PhysicalNetworkFunctionDescriptor id
	 */
	public String deletePhysicalNetworkFunctionDescriptor(final String id, final String id_pnf) {
		return "IMAGE UPDATED";
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
	public String postPhysicalNetworkFunctionDescriptor(final File pnf, final String id, final String id_pnf) {
		return "IMAGE UPDATED";
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
	public String updatePNFD(final File pnf, final String id, final String id_pnf) {
		return "IMAGE UPDATED";
	}

	/**
	 * Returns the Security into a NSD with id
	 *
	 * @param id
	 *            : The id of NSD
	 * @return Security: The Security of PhysicalNetworkFunctionDescriptor into
	 *         NSD
	 */
	public String getSecurity(final String id) {
		return "IMAGE UPDATED";
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
	public String getSecurity(final String id, final String id_s) {
		return "IMAGE UPDATED";
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
	public String deleteSecurity(final String id, final String id_s) {
		return "IMAGE UPDATED";
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
	public String postSecurity(final File security, final String id) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String updateSecurity(final File security, final String id, final String id_s) {
		return "IMAGE UPDATED";
	}

	/**
	 *
	 */
	public String createRecord(final File networkServiceDescriptor) {
		return "IMAGE UPDATED";
	}
}
