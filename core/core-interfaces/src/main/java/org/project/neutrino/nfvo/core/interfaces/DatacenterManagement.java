package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.Datacenter;

import java.util.List;

/**
 * Created by lto on 13/05/15.
 */
public interface DatacenterManagement {

    /**
     * This operation allows adding a datacenter
     * into the datacenter repository.
     * @param datacenter
     */
    Datacenter add(Datacenter datacenter);

    /**
     * This operation allows deleting the datacenter
     * from the datacenter repository.
     * @param id
     */
    void delete(String id);

    /**
     * This operation allows updating the datacenter
     * in the datacenter repository.
     * @param new_datacenter
     * @param id
     */
    Datacenter update(Datacenter new_datacenter, String id);

    /**
     * This operation allows querying the information of
     * the datacenters in the datacenter repository.
     */
    List<Datacenter> query();

    /**
     * This operation allows querying the information of
     * the datacenter in the datacenter repository.
     */
    Datacenter query(String id);

}
