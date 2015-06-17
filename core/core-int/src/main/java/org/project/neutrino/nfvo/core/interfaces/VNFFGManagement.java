package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;

import java.util.List;

/**
 * Created by lto on 16/06/15.
 */
public interface VNFFGManagement {
    VNFForwardingGraphDescriptor add(VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor);

    void delete(String id);

    List<VNFForwardingGraphDescriptor> query();

    VNFForwardingGraphDescriptor query(String id);

    VNFForwardingGraphDescriptor update(VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor, String id);
}
