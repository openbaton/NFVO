package org.project.neutrino.nfvo.vim_interfaces;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;

import java.util.List;

/**
 * Created by lto on 03/06/15.
 */
public interface DeploymentFlavorManagement {
    /**
     * This operation allows adding new DeploymentFlavor
     *  to the repository.
     * @param deploymentFlavour
     */
    DeploymentFlavour add(DeploymentFlavour deploymentFlavour);

    /**
     * This operation allows deleting in the DeploymentFlavour
     * from the repository.
     * @param id
     */
    void delete(String id);

    /**
     * This operation allows updating the DeploymentFlavour
     * in the repository.
     */
    DeploymentFlavour update(DeploymentFlavour new_deploymentFlavour);

    /**
     * This operation allows querying the information of
     * the DeploymentFlavours in the repository.
     */
    List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance);

}
