package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ReleaseresourcesTask extends AbstractTask {

    @Autowired
    private ResourceManagement resourceManagement;

    @Override
    protected void doWork() throws Exception {
        log.debug("Released resources for VNFR: " + virtualNetworkFunctionRecord.getName());
//        virtualNetworkFunctionRecord.setStatus(Status.TERMINATED);
        saveVirtualNetworkFunctionRecord();

        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
            log.debug("Removing VDU: " + virtualDeploymentUnit.getHostname());
                this.resourceManagement.release(virtualDeploymentUnit);
        }

    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
