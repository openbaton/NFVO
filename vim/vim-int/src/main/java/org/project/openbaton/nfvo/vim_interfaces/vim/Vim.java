package org.project.openbaton.nfvo.vim_interfaces.vim;

import org.project.openbaton.nfvo.vim_interfaces.flavor_management.DeploymentFlavorManagement;
import org.project.openbaton.nfvo.vim_interfaces.image_management.ImageManagement;
import org.project.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement;
import org.project.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement;

/**
 * Created by mpa on 12.06.15.
 */
public interface Vim extends ImageManagement, ResourceManagement, NetworkManagement, DeploymentFlavorManagement {
}
