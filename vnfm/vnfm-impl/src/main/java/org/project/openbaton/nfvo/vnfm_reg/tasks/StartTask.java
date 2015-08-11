package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class StartTask extends AbstractTask {

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void doWork() throws Exception {
        log.debug("----> STARTED VNFR: " + virtualNetworkFunctionRecord.getName());
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
    }
}
