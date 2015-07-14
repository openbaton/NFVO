package org.project.openbaton.nfvo.vim_interfaces.flavor_management;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.exceptions.VimException;

import java.util.List;

/**
 * Created by lto on 03/06/15.
 */
public interface DeploymentFlavorManagement {
    /**
     * This operation allows adding new DeploymentFlavor
     * to the repository.
     * @param vimInstance
     * @param deploymentFlavour
     */
    DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException;

    /**
     * This operation allows deleting in the DeploymentFlavour
     * from the repository.
     * @param vimInstance
     * @param deploymentFlavor
     */
    void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException;

    /**
     * This operation allows updating the DeploymentFlavour
     * in the repository.
     * @param vimInstance
     * @param deploymentFlavour
     */
    DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException;

    /**
     * This operation allows querying the information of
     * the DeploymentFlavours in the repository.
     * @param vimInstance
     */
    List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) throws VimException;

}
