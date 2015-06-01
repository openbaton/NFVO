package org.project.neutrino.nfvo.vnfm_reg;

import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
@Service
@Scope
public class VnfmManager implements org.project.neutrino.vnfm.interfaces.manager.VnfmManager {

    @Autowired
    @Qualifier("vnfmRegister")
    private org.project.neutrino.vnfm.interfaces.register.VnfmRegister vnfmRegister;

    @Override
    @Async
    public Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException {
        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
            VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(vnfr.getType());
            if (endpoint == null){
                throw new NotFoundException("VnfManager of type " + vnfr.getType() + " is not registered");
            }

            /**
             *  TODO Here use an abstraction to call the particular vnfm_reg
             */

        }
        return new AsyncResult<Void>(null);
    }
}
