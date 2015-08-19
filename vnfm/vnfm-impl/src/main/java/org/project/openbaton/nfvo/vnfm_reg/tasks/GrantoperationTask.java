package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class GrantoperationTask extends AbstractTask {

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    @Autowired
    private VNFLifecycleOperationGranting lifecycleOperationGranting;

    @Override
    protected void doWork() throws Exception {

        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
        CoreMessage message = null;
        if (lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord)) {
            LifecycleEvent lifecycleEvent = new LifecycleEvent();
            lifecycleEvent.setEvent(Event.GRANTED);
            if (virtualNetworkFunctionRecord.getLifecycle_event_history() == null)
                virtualNetworkFunctionRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
            virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
            message = new CoreMessage();
            message.setAction(Action.GRANT_OPERATION);
            message.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
            log.debug("Verison is: " + virtualNetworkFunctionRecord.getHb_version());
            vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
        } else {
            message.setAction(Action.ERROR);
            vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
