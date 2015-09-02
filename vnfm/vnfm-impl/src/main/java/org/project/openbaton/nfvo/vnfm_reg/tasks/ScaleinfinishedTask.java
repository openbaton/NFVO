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
public class ScaleinfinishedTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: SCALE_IN_FINISHED");
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord_nfvo = vnfrRepository.find(virtualNetworkFunctionRecord.getId());
        virtualNetworkFunctionRecord_nfvo.setStatus(virtualNetworkFunctionRecord.getStatus());
        List<String> existingVDUs = new ArrayList<>();
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            existingVDUs.add(vdu.getId());
        }
        Set<VirtualDeploymentUnit> removedVdus = new HashSet<>();
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord_nfvo.getVdu()) {
            if (!existingVDUs.contains(vdu.getId())) {
                removedVdus.add(vdu);
            }
        }
        virtualNetworkFunctionRecord_nfvo.getVdu().removeAll(removedVdus);
        Set<String> old_addresses = new HashSet<>();
        for (String ip : virtualNetworkFunctionRecord_nfvo.getVnf_address()) {
            if (!virtualNetworkFunctionRecord.getVnf_address().contains(ip)) {
                old_addresses.add(ip);
            }
        }
        virtualNetworkFunctionRecord_nfvo.getVnf_address().removeAll(old_addresses);
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord_nfvo);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
