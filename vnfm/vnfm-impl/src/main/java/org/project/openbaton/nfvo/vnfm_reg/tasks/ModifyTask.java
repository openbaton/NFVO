package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
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
public class ModifyTask extends AbstractTask {

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;


    @Override
    protected void doWork() throws Exception {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        log.debug("NFVO: MODIFY finish");
        log.trace("VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        log.debug("STATE IS: " + virtualNetworkFunctionRecord.getStatus());
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
        log.debug("VNFR Status is: " + virtualNetworkFunctionRecord.getStatus());

        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(Action.START);
        coreMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
        vnfmSender.sendCommand(coreMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
