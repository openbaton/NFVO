package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ConfigureTask extends AbstractTask {

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;


    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void doWork() throws Exception {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());
        log.debug("----> CONFIGURE finished for VNFR: " + virtualNetworkFunctionRecord.getName());
        log.debug("Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
        virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord);
        vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord,Action.INSTANTIATE), vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
    }
}
