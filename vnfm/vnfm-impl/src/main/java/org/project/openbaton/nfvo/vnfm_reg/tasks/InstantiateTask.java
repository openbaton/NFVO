package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class InstantiateTask extends AbstractTask {

    @Autowired
    private DependencyManagement dependencyManagement;

    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: instantiate finish");
        log.trace("Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
        log.info("Instantiation is finished for vnfr: " + virtualNetworkFunctionRecord.getName());
        log.debug("Calling dependency management for VNFR: " + virtualNetworkFunctionRecord.getName());
        int dep = 0;
        try {
            dep = dependencyManagement.provisionDependencies(virtualNetworkFunctionRecord);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (dep == 0) {
            log.info("VNFR: " + virtualNetworkFunctionRecord.getName() + " (" + virtualNetworkFunctionRecord.getId() + ") has 0 dependencies, setting status to ACTIVE");
            virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
            virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
