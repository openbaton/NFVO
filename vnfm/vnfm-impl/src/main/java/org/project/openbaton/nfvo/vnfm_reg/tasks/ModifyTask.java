package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
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
public class ModifyTask extends AbstractTask {

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    @Autowired
    @Qualifier("VNFRDependencyRepository")
    private GenericRepository<VNFRecordDependency> vnfrDependencyRepository;


    @Override
    protected void doWork() throws Exception {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

//        dependency.setStatus(Status.ACTIVE);
//        dependency = vnfrDependencyRepository.merge(dependency);

        virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
        log.debug("NFVO: MODIFY finish");
        log.trace("VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
        log.trace("Now VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        log.debug("VNFR Status is: " + virtualNetworkFunctionRecord.getStatus());

        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(Action.START);
        coreMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
        vnfmSender.sendCommand(coreMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
