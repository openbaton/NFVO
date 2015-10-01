package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by mpa on 01.10.15.
 */
@Service
@Scope
public class VirtualNetworkFunctionManagement implements org.openbaton.nfvo.core.interfaces.VirtualNetworkFunctionManagement {

    @Autowired
    private VNFDRepository vnfdRepository;

    @Override
    public VirtualNetworkFunctionDescriptor add(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor) {
        // TODO check integrity of VNFD
        return vnfdRepository.save(virtualNetworkFunctionDescriptor);
    }

    @Override
    public void delete(String id) {
        vnfdRepository.delete(id);
    }

    @Override
    public Iterable<VirtualNetworkFunctionDescriptor> query() {
        return vnfdRepository.findAll();
    }

    @Override
    public VirtualNetworkFunctionDescriptor query(String id) {
        return vnfdRepository.findFirstById(id);
    }

    @Override
    public VirtualNetworkFunctionDescriptor update(VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String id) {
        //TODO Update inner fields
        return vnfdRepository.save(virtualNetworkFunctionDescriptor);
    }
}
