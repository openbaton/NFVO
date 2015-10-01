package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;

/**
 * Created by mpa on 01.10.15.
 */
public interface VirtualNetworkFunctionManagement {
    VirtualNetworkFunctionDescriptor add(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor);

    void delete(String id);

    Iterable<VirtualNetworkFunctionDescriptor> query();

    VirtualNetworkFunctionDescriptor query(String id);

    VirtualNetworkFunctionDescriptor update(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String id);
}
