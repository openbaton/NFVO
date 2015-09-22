package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.project.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.project.openbaton.nfvo.core.interfaces.DependencyQueuer;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
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

    @Autowired
    private DependencyQueuer dependencyQueuer;

    @Override
    protected void doWork() throws Exception {

        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        log.info("Instantiation is finished for vnfr: " + virtualNetworkFunctionRecord.getName() + " his nsr id father is:" + virtualNetworkFunctionRecord.getParent_ns_id());
        for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()) {
            if (lifecycleEvent.getEvent().ordinal() == Event.CONFIGURE.ordinal())
                log.info("THE EVENT CONFIGURE HAS THIS SCRIPTS: " + lifecycleEvent.getLifecycle_events());
        }
        saveVirtualNetworkFunctionRecord();

        for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()) {
            if (lifecycleEvent.getEvent().ordinal() == Event.CONFIGURE.ordinal())
                log.info("THE EVENT CONFIGURE HAS THIS SCRIPTS: " + lifecycleEvent.getLifecycle_events());
        }

        dependencyManagement.fillParameters(virtualNetworkFunctionRecord);

        NetworkServiceRecord nsr = networkServiceRecordRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
        for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr())
            log.debug("Current Vnfrs in the database: "+vnfr.getName());
        dependencyQueuer.releaseVNFR(virtualNetworkFunctionRecord.getName(),nsr);
        for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()) {
            if (lifecycleEvent.getEvent().ordinal() == Event.CONFIGURE.ordinal())
                log.info("THE EVENT CONFIGURE HAS THIS SCRIPTS: " + lifecycleEvent.getLifecycle_events());
        }
        log.debug("Calling dependency management for VNFR: " + virtualNetworkFunctionRecord.getName());
        int dep;
        dep = dependencyManagement.provisionDependencies(virtualNetworkFunctionRecord);
        for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()) {
            if (lifecycleEvent.getEvent().ordinal() == Event.CONFIGURE.ordinal())
                log.info("THE EVENT CONFIGURE HAS THIS SCRIPTS: " + lifecycleEvent.getLifecycle_events());
        }
        log.debug("Found " + dep + " dependencies");
        if (dep == 0) {
            log.info("VNFR: " + virtualNetworkFunctionRecord.getName() + " (" + virtualNetworkFunctionRecord.getId() + ") has 0 dependencies, Calling START");
            vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord,Action.START), vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
        }
    }



    @Override
    public boolean isAsync() {
        return true;
    }
}
