package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

import java.util.Set;

/**
 * Created by mob on 15.09.15.
 */
public class VnfmOrAllocateResourcesMessage implements VnfmOrMessage {
    private VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;
    private Set<VirtualDeploymentUnit> vduSet;

    public VnfmOrAllocateResourcesMessage(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, Set<VirtualDeploymentUnit> vduSet) {
        this.virtualNetworkFunctionDescriptor = virtualNetworkFunctionDescriptor;
        this.vduSet = vduSet;
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

    @Override
    public String toString() {
        return "VnfmOrAllocateResourcesMessage{" +
                "virtualNetworkFunctionDescriptor=" + virtualNetworkFunctionDescriptor +
                ", vduSet=" + vduSet +
                '}';
    }

    @Override
    public Action getAction() {
        return Action.ALLOCATE_RESOURCES;
    }
}
