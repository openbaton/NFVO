package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

import java.util.Set;

/**
 * Created by mob on 15.09.15.
 */
public class OrVnfmAllocateResourcesMessage implements OrVnfmMessage {
    private Set<VirtualDeploymentUnit> vduSet;

    public OrVnfmAllocateResourcesMessage(Set<VirtualDeploymentUnit> vduSet) {
        this.vduSet = vduSet;
    }

    public Set<VirtualDeploymentUnit> getVduSet() {
        return vduSet;
    }

    public void setVduSet(Set<VirtualDeploymentUnit> vduSet) {
        this.vduSet = vduSet;
    }

    @Override
    public String toString() {
        return "OrVnfmAllocateResourcesMessage{" +
                "vduSet=" + vduSet +
                '}';
    }

    @Override
    public Action getAction() {
        return Action.ALLOCATE_RESOURCES;
    }
}
