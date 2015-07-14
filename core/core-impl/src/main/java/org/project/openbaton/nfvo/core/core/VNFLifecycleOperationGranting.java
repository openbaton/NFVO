package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Quota;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.exceptions.VimException;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Created by lto on 11/06/15.
 */
@Service
@Scope
public class VNFLifecycleOperationGranting implements org.project.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting {
    @Autowired
    private VimBroker vimBroker;

    @Override
    public boolean grantLifecycleOperation(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimException {
        //HashMap holds how many VDUs are deployed on a specific VimInstance
        HashMap<VimInstance,Integer> countVDUsOnVimInstances = new HashMap<>();
        //Count VDUs on a specific VimInstance
        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            if (vdu.getExtId() != null)
                break;
            if (countVDUsOnVimInstances.containsKey(vdu.getVimInstance())) {
                countVDUsOnVimInstances.put(vdu.getVimInstance(), countVDUsOnVimInstances.get(vdu.getVimInstance()) + 1);
            } else {
                countVDUsOnVimInstances.put(vdu.getVimInstance(), 1);
            }
        }
        //Check if enough resources are available for the deployment
        for (VimInstance vimInstance : countVDUsOnVimInstances.keySet()) {
            Quota leftQuota = vimBroker.getLeftQuota(vimInstance);
            //Fetch the Flavor for getting allocated resources needed
            DeploymentFlavour flavor = null;
            for (DeploymentFlavour currentFlavor : vimInstance.getFlavours()) {
                if (currentFlavor.getFlavour_key().equals(virtualNetworkFunctionRecord.getDeployment_flavour_key())) {
                    flavor = currentFlavor;
                    break;
                }
            }
            //Subtract needed resources from the left resources
            for (int i = 1; i <= countVDUsOnVimInstances.get(vimInstance); i++) {
                leftQuota.setInstances(leftQuota.getInstances() - 1);
                leftQuota.setCores(leftQuota.getCores() - flavor.getVcpus());
                leftQuota.setRam(leftQuota.getRam() - flavor.getRam());
            }
            //If one value is negative, it is not possible to deploy the VNFR on (at least on one VimInstance) -> return false
            if (leftQuota.getInstances() < 0 || leftQuota.getRam() < 0 || leftQuota.getCores() < 0)
                return false;
        }
        //If there are enough resources to deploy the VNFR on the VimInstance, return true
        return true;
    }
}
