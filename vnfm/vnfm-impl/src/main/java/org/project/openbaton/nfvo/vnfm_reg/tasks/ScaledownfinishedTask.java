package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ScaledownfinishedTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: SCALE_DOWN_FINISHED");
        log.debug("The VNFR: " + virtualNetworkFunctionRecord.getName() + " shoud be in status Active --> " + virtualNetworkFunctionRecord.getStatus());
//        virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);

        /**
         * still, why?
         */
        List<String> existingVDUs = new ArrayList<String>();
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            existingVDUs.add(vdu.getId());
        }
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
