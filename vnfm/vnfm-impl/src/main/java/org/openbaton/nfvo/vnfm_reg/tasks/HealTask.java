package org.openbaton.nfvo.vnfm_reg.tasks;

import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.nfvo.repositories.VNFRDependencyRepository;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by mob on 03.12.15.
 */
@Service
@Scope("prototype")
public class HealTask extends AbstractTask {

    @Override
    protected NFVMessage doWork() throws Exception {
        VnfmSender vnfmSender;

        log.debug("NFVO: HEAL finished");

        saveVirtualNetworkFunctionRecord();

        log.debug("VNFR Status is: " + virtualNetworkFunctionRecord.getStatus());

        return null;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}