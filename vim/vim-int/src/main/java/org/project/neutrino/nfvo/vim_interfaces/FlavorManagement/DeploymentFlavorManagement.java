package org.project.neutrino.nfvo.vim_interfaces.FlavorManagement;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.common.exceptions.VimException;

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
