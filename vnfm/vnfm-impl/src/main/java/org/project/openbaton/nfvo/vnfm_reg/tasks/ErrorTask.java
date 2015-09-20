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
public class ErrorTask extends AbstractTask {

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void doWork() throws Exception {
        log.error("----> ERROR for VNFR: " + virtualNetworkFunctionRecord.getName());
        virtualNetworkFunctionRecord.setStatus(Status.ERROR);
        saveVirtualNetworkFunctionRecord();
    }
}
