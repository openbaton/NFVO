package org.project.neutrino.nfvo.core.vnfm;

import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.core.interfaces.exception.NotFoundException;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 26/05/15.
 */
@Service
@Scope
public class VnfmRegister implements org.project.neutrino.nfvo.core.interfaces.VnfmRegister {

    @Autowired
    @Qualifier("vnfmEndpointRepository")
    private GenericRepository<VnfmManagerEndpoint> vnfmManagerEndpointRepository;

    @Override
    public List<VnfmManagerEndpoint> listVnfm() {
        return this.vnfmManagerEndpointRepository.findAll();
    }


    protected void register(String type, String endpoint, String endpointType) {
        this.vnfmManagerEndpointRepository.create(new VnfmManagerEndpoint(type, endpoint, endpointType));
    }

    protected void register(VnfmManagerEndpoint endpoint) {
        this.vnfmManagerEndpointRepository.create(endpoint);
    }

    @Override
    public void addManagerEndpoint(VnfmManagerEndpoint endpoint){
        throw new UnsupportedOperationException();
    };

    @Override
    public VnfmManagerEndpoint getVnfm(String type) throws NotFoundException {
        for (VnfmManagerEndpoint vnfmManagerEndpoint : this.vnfmManagerEndpointRepository.findAll()){
            if (vnfmManagerEndpoint.getType().toLowerCase().equals(type.toLowerCase())){
                return vnfmManagerEndpoint;
            }
        }
        throw new NotFoundException("VnfManager of type " + type + " is not registered");
    }
}
