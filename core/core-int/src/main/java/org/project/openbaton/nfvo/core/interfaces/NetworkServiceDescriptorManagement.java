package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;

import javax.persistence.NoResultException;
import java.util.List;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkServiceDescriptorManagement {

	/**
	 * This operation allows submitting and
	 * validating a Network Service	Descriptor (NSD), 
	 * including any related VNFFGD and VLD.
	 */
	NetworkServiceDescriptor onboard(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException;

	/**
	 * This operation allows disabling a
	 * Network Service Descriptor, so that it
	 * is not possible to instantiate it any 
	 * further.
	 * @param id
	 */
	boolean disable(String id);

	/**
	 * This operation allows enabling a
	 * Network Service Descriptor.
	 * @param id
	 */
	boolean enable(String id);

	/**
	 * This operation allows updating a Network 
	 * Service Descriptor (NSD), including any 
	 * related VNFFGD and VLD.This update might 
	 * include creating/deleting new VNFFGDs
	 * and/or new VLDs.
	 * @param new_nsd
	 * @param old_id
	 */
	NetworkServiceDescriptor update(NetworkServiceDescriptor new_nsd, String old_id);

	/**
	 * This operation is used to query the
	 * information of the Network Service
	 * Descriptor (NSD), including any
	 * related VNFFGD and VLD.
	 */
	List<NetworkServiceDescriptor> query();

	NetworkServiceDescriptor query(String id) throws NoResultException;

	/**
	 * This operation is used to remove a
	 * disabled Network Service Descriptor.
	 * @param id
	 */
	void delete(String id);

}
