package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.common.exceptions.BadFormatException;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.common.exceptions.VimException;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkServiceRecordManagement {

	/**
	 * This operation allows submitting and
	 * validating a Network Service	Descriptor (NSD),
	 * including any related VNFFGD and VLD.
	 */
	NetworkServiceRecord onboard(String nsd_id) throws InterruptedException, ExecutionException, NamingException, VimException, JMSException, NotFoundException, BadFormatException;

	/**
	 * This operation allows submitting and
	 * validating a Network Service	Descriptor (NSD),
	 * including any related VNFFGD and VLD.
	 */
	NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) throws ExecutionException, InterruptedException, VimException, NotFoundException, NotFoundException, JMSException, NamingException, BadFormatException;

	/**
	 * This operation allows updating a Network 
	 * Service Descriptor (NSD), including any 
	 * related VNFFGD and VLD.This update might 
	 * include creating/deleting new VNFFGDs
	 * and/or new VLDs.
	 * @param new_nsd
	 * @param old_id
	 */
	NetworkServiceRecord update(NetworkServiceRecord new_nsd, String old_id);

	/**
	 * This operation is used to query the
	 * information of the Network Service
	 * Descriptor (NSD), including any
	 * related VNFFGD and VLD.
	 */
	List<NetworkServiceRecord> query();

	NetworkServiceRecord query(String id);

	/**
	 * This operation is used to remove a
	 * disabled Network Service Descriptor.
	 * @param id
	 */
	void delete(String id) throws VimException, JMSException, NamingException, NotFoundException, ExecutionException, InterruptedException;

}
