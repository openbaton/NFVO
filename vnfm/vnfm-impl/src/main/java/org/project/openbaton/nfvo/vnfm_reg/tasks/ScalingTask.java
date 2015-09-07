package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ScalingTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: SCALING");
        log.debug("The VNFR: " + virtualNetworkFunctionRecord.getName() + " shoud be in status scaling --> " + virtualNetworkFunctionRecord.getStatus());
//        virtualNetworkFunctionRecord.setStatus(Status.SCALING);
        virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
