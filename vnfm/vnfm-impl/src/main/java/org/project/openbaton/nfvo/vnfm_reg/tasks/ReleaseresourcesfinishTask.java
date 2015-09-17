package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ReleaseresourcesfinishTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("Released resources for VNFR: " + virtualNetworkFunctionRecord.getName());
//        virtualNetworkFunctionRecord.setStatus(Status.TERMINATED);
        saveVirtualNetworkFunctionRecord();
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
