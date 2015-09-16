package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

import java.util.Set;

/**
 * Created by mob on 15.09.15.
 */
public class VnfmOrGrantLifecycleOperationMessage implements VnfmOrMessage {
    private VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;
    private Set<VirtualDeploymentUnit> vduSet;
    private String deploymentFlavourKey;

    public VnfmOrGrantLifecycleOperationMessage(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, Set<VirtualDeploymentUnit> vduSet, String deploymentFlavourKey) {

        this.virtualNetworkFunctionDescriptor = virtualNetworkFunctionDescriptor;
        this.vduSet = vduSet;
        this.deploymentFlavourKey = deploymentFlavourKey;
    }

    public VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor() {
        return virtualNetworkFunctionDescriptor;
    }

    public void setVirtualNetworkFunctionDescriptor(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor) {
        this.virtualNetworkFunctionDescriptor = virtualNetworkFunctionDescriptor;
    }

    public Set<VirtualDeploymentUnit> getVduSet() {
        return vduSet;
    }

    public void setVduSet(Set<VirtualDeploymentUnit> vduSet) {
        this.vduSet = vduSet;
    }

    public String getDeploymentFlavourKey() {
        return deploymentFlavourKey;
    }

    public void setDeploymentFlavourKey(String deploymentFlavourKey) {
        this.deploymentFlavourKey = deploymentFlavourKey;
    }

    @Override
    public String toString() {
        return "VnfmOrGrantLifecycleOperationMessage{" +
                "virtualNetworkFunctionDescriptor='" + virtualNetworkFunctionDescriptor + '\'' +
                ", vduSet=" + vduSet +
                ", deploymentFlavourKey='" + deploymentFlavourKey + '\'' +
                '}';
    }

    @Override
    public Action getAction() {
        return Action.GRANT_OPERATION;
    }
}
