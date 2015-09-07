package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class AllocateresourcesTask extends AbstractTask {
    @Autowired
    private ResourceManagement resourceManagement;

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    @Override
    protected void doWork() throws Exception {

        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        log.debug("NFVO: ALLOCATE_RESOURCES");
        List<Future<String>> ids = new ArrayList<>();
        boolean error = false;
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu())
            try {
                String extId = resourceManagement.allocate(vdu, virtualNetworkFunctionRecord);
                log.debug("the returned ext id is: " + extId);
                vdu.setExtId(extId);
            } catch (VimException e) {
                e.printStackTrace();
                log.error(e.getMessage());
                CoreMessage errorMessage = new CoreMessage();
                errorMessage.setAction(Action.ERROR);
                LifecycleEvent lifecycleEvent = new LifecycleEvent();
                lifecycleEvent.setEvent(Event.ERROR);
                virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
                errorMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
                    vnfmSender.sendCommand(errorMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
                error = true;
            } catch (VimDriverException e) {
                e.printStackTrace();
                log.error(e.getMessage());
                CoreMessage message = new CoreMessage();
                message.setAction(Action.ERROR);
                LifecycleEvent lifecycleEvent = new LifecycleEvent();
                lifecycleEvent.setEvent(Event.ERROR);
                virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
                virtualNetworkFunctionRecord.setStatus(Status.ERROR);
                virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord);
                message.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
                    vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
                error = true;
            }

        for (LifecycleEvent event : virtualNetworkFunctionRecord.getLifecycle_event()) {
            if (event.getEvent().ordinal() == Event.ALLOCATE.ordinal()) {
                virtualNetworkFunctionRecord.getLifecycle_event_history().add(event);
//                virtualNetworkFunctionRecord.getLifecycle_event().remove(event);
                break;
            }
        }

        if (!error) {


            CoreMessage coreMessage = new CoreMessage();
            coreMessage.setAction(Action.INSTANTIATE);
            virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord);

            for (VirtualDeploymentUnit virtualDeploymentUnit: virtualNetworkFunctionRecord.getVdu()){
                log.debug(">---< The unit ext id is: " + virtualDeploymentUnit.getExtId());
            }
            coreMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
            vnfmSender.sendCommand(coreMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
