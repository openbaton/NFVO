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
public class ScaleoutfinishedTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: SCALE_OUT_FINISHED");
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord_nfvo = vnfrRepository.find(virtualNetworkFunctionRecord.getId());
        virtualNetworkFunctionRecord_nfvo.setStatus(virtualNetworkFunctionRecord.getStatus());
        List<String> existingVDUs = new ArrayList<String>();
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord_nfvo.getVdu()) {
            existingVDUs.add(vdu.getId());
        }
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            if (!existingVDUs.contains(vdu.getId())) {
                virtualNetworkFunctionRecord_nfvo.getVdu().add(vdu);
            }
        }
        Set<String> new_addresses = new HashSet<>();
        for (String ip : virtualNetworkFunctionRecord.getVnf_address()) {
            if (!virtualNetworkFunctionRecord_nfvo.getVnf_address().contains(ip)) {
                new_addresses.add(ip);
            }
        }
        virtualNetworkFunctionRecord_nfvo.getVnf_address().addAll(new_addresses);
        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord_nfvo);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
