package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.nfvo.catalogue.nfvo.Configuration;

import java.util.List;

/**
 * Created by lto on 13/05/15.
 */
public interface ConfigurationManagement {

    /**
     * This operation allows adding a datacenter
     * into the datacenter repository.
     * @param datacenter
     */
    Configuration add(Configuration datacenter);

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
    Configuration update(Configuration new_datacenter, String id);

    /**
     * This operation allows querying the information of
     * the datacenters in the datacenter repository.
     */
    List<Configuration> query();

    /**
     * This operation allows querying the information of
     * the datacenter in the datacenter repository.
     */
    Configuration query(String id);

}
