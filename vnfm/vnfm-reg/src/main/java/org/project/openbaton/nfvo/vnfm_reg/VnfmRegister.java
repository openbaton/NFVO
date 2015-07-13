package org.project.openbaton.nfvo.vnfm_reg;

import org.project.openbaton.common.catalogue.nfvo.EndpointType;
import org.project.openbaton.common.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 26/05/15.
 */
@Service
@Scope
public class VnfmRegister implements org.project.openbaton.vnfm.interfaces.register.VnfmRegister {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("vnfmEndpointRepository")
    private GenericRepository<VnfmManagerEndpoint> vnfmManagerEndpointRepository;

    @Override
    public List<VnfmManagerEndpoint> listVnfm() {
        return this.vnfmManagerEndpointRepository.findAll();
    }


    protected void register(String type, String endpoint, EndpointType endpointType) {
        this.vnfmManagerEndpointRepository.create(new VnfmManagerEndpoint(type, endpoint, endpointType));
    }

    protected void register(VnfmManagerEndpoint endpoint) {
        log.debug("Perisisting: " + endpoint);
        this.vnfmManagerEndpointRepository.create(endpoint);
    }

    @Override
    public void addManagerEndpoint(VnfmManagerEndpoint endpoint) {
        throw new UnsupportedOperationException();
    }

    public void removeManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VnfmManagerEndpoint getVnfm(String type) throws NotFoundException {
        for (VnfmManagerEndpoint vnfmManagerEndpoint : this.vnfmManagerEndpointRepository.findAll()){
            log.trace(""+vnfmManagerEndpoint);
            log.trace("" + type);
            if (vnfmManagerEndpoint.getType().toLowerCase().equals(type.toLowerCase())){
                return vnfmManagerEndpoint;
            }
        }
        throw new NotFoundException("VnfManager of type " + type + " is not registered");
    }

    public void unregister(VnfmManagerEndpoint endpoint) {
        List<VnfmManagerEndpoint> vnfmManagerEndpoints = vnfmManagerEndpointRepository.findAll();
        for (VnfmManagerEndpoint vnfmManagerEndpoint: vnfmManagerEndpoints){
            if (vnfmManagerEndpoint.getEndpoint().equals(endpoint.getEndpoint()) && vnfmManagerEndpoint.getEndpointType().equals(endpoint.getEndpointType()) && vnfmManagerEndpoint.getType().equals(endpoint.getType())){
                this.vnfmManagerEndpointRepository.remove(endpoint);
                return;
            }
        }
        log.error("no VNFM found for endpoint: " + endpoint);
    }
}
