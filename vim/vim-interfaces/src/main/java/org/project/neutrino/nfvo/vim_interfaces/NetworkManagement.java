package org.project.neutrino.nfvo.vim_interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;

import java.util.List;


/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkManagement {
    
	/**
     * This operation allows adding new VNF software 
     * images to the image repository.
     */
    Network add(Network network);

    /**
	 * This operation allows deleting in the VNF software 
	 * images from the image repository.
     * @param id
     */
    void delete(String id);
    
    /**
	 * This operation allows updating the VNF software 
	 * images in the image repository.
     * @param new_network
     * @param id
     */
    Network update(Network new_network, String id);
    
    /**
	 * This operation allows querying the information of 
	 * the VNF software images in the image repository.
     * @param vimInstance
     */
    List<Network> queryNetwork(VimInstance vimInstance);
    
    /**
     * This operation allows querying the information of 
     * the VNF software image in the image repository.
     */
    Network query(String id);
}
