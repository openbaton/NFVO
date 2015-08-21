package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.project.openbaton.nfvo.core.interfaces.DependencyQueuer;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class InstantiateTask extends AbstractTask {

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    @Autowired
    private DependencyManagement dependencyManagement;
    @Autowired

    private DependencyQueuer dependencyQueuer;

    @Override
    protected void doWork() throws Exception {

        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        log.debug("NFVO: instantiate finish");
        log.trace("Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
        log.trace("now Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        log.info("Instantiation is finished for vnfr: " + virtualNetworkFunctionRecord.getName());
        log.info("Releasing waiting VNFRs");
        dependencyQueuer.releaseVNFR(virtualNetworkFunctionRecord.getName());
        log.debug("Calling dependency management for VNFR: " + virtualNetworkFunctionRecord.getName());
        int dep;
        dep = dependencyManagement.provisionDependencies(virtualNetworkFunctionRecord);
        log.debug("Found " + dep + " dependencies");
        if (dep == 0) {
            log.info("VNFR: " + virtualNetworkFunctionRecord.getName() + " (" + virtualNetworkFunctionRecord.getId() + ") has 0 dependencies, Calling START");
            CoreMessage coreMessage = new CoreMessage();
            coreMessage.setAction(Action.START);
            coreMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
            vnfmSender.sendCommand(coreMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
