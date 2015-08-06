package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ModifyTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: MODIFY finish");
        log.trace("VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
        log.debug("VNFR Status is: " + virtualNetworkFunctionRecord.getStatus());
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
