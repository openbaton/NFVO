package org.project.neutrino.nfvo.interfaces;

/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkServiceDiscriptorManagement {

	/**
	 * This operation allows submitting and
	 * validating a Network Service	Descriptor (NSD), 
	 * including any related VNFFGD and VLD.
	 */
	void onboard();

	/**
	 * This operation allows disabling a
	 * Network Service Descriptor, so that it
	 * is not possible to instantiate it any 
	 * further.
	 */
	void disable();

	/**
	 * This operation allows enabling a
	 * Network Service Descriptor.
	 */
	void enable();

	/**
	 * This operation allows updating a Network 
	 * Service Descriptor (NSD), including any 
	 * related VNFFGD and VLD.This update might 
	 * include creating/deleting new VNFFGDs
	 * and/or new VLDs.
	 */
	void update();

	/**
	 * This operation is used to query the
	 * information of the Network Service
	 * Descriptor (NSD), including any
	 * related VNFFGD and VLD.
	 */
	void query();

	/**
	 * This operation is used to remove a
	 * disabled Network Service Descriptor.
	 */
	void delete();	

}
