package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ScaledownfinishedTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: SCALE_DOWN_FINISHED");
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
