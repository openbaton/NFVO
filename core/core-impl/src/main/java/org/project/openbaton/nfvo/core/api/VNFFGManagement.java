package org.project.openbaton.nfvo.core.api;

import org.project.openbaton.nfvo.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 16/06/15.
 */
@Service
@Scope
public class VNFFGManagement implements org.project.openbaton.nfvo.core.interfaces.VNFFGManagement {

    @Autowired
    @Qualifier("VNFFGDescriptorRepository")
    private GenericRepository<VNFForwardingGraphDescriptor> vnffgDescriptorRepository;

    @Override
    public VNFForwardingGraphDescriptor add(VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor) {
        return vnffgDescriptorRepository.create(vnfForwardingGraphDescriptor);
    }

    @Override
    public void delete(String id) {
        vnffgDescriptorRepository.remove(vnffgDescriptorRepository.find(id));
    }

    @Override
    public List<VNFForwardingGraphDescriptor> query() {
        return vnffgDescriptorRepository.findAll();
    }

    @Override
    public VNFForwardingGraphDescriptor query(String id) {
        return vnffgDescriptorRepository.find(id);
    }

    @Override
    public VNFForwardingGraphDescriptor update(VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor, String id) {
        throw new UnsupportedOperationException();
    }
}
