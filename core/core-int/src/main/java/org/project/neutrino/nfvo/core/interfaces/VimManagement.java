package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;

import java.util.List;

/**
 * Created by lto on 13/05/15.
 */
public interface VimManagement {

    /**
     * This operation allows adding a datacenter
     * into the datacenter repository.
     * @param vimInstance
     */
    VimInstance add(VimInstance vimInstance) throws VimException;

    /**
     * This operation allows deleting the datacenter
     * from the datacenter repository.
     * @param id
     */
    void delete(String id);

    /**
     * This operation allows updating the datacenter
     * in the datacenter repository.
     * @param new_vimInstance
     * @param id
     */
    VimInstance update(VimInstance new_vimInstance, String id) throws VimException;

    /**
     * This operation allows querying the information of
     * the datacenters in the datacenter repository.
     */
    List<VimInstance> query();

    /**
     * This operation allows querying the information of
     * the datacenter in the datacenter repository.
     */
    VimInstance query(String id);

    void refresh(VimInstance vimInstance) throws VimException;
}
