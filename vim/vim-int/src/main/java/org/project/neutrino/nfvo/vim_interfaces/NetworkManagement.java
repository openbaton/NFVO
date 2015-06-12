package org.project.neutrino.nfvo.vim_interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.common.exceptions.VimException;

import java.util.List;


/**
 * Created by mpa on 30/04/15.
 */

public interface NetworkManagement {
    
	/**
     * This operation allows adding new Network
     * to the network repository.
     * @param vimInstance
     * @param network
     */
    Network add(VimInstance vimInstance, Network network) throws VimException;

    /**
	 * This operation allows deleting in the Networks
     * from the network repository.
     * @param vimInstance
     * @param network
     */
    void delete(VimInstance vimInstance, Network network) throws VimException;
    
    /**
	 * This operation allows updating the Network
     * in the network repository.
     * @param vimInstance
     * @param updatingNetwork
     */
    Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException;
    
    /**
	 * This operation allows querying the information of 
	 * the Networks in the network repository.
     * @param vimInstance
     */
    List<Network> queryNetwork(VimInstance vimInstance) throws VimException;
    
    /**
     * This operation allows querying the information of 
     * the Networks in the network repository.
     * @param vimInstance
     */
    Network query(VimInstance vimInstance, String extId) throws VimException;
}
