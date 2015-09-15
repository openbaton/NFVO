package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ScaledTask extends AbstractTask {
    @Override
    protected void doWork() throws Exception {
        log.debug("NFVO: SCALED");
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord_nfvo = vnfrRepository.findOne(virtualNetworkFunctionRecord.getId());
        //Updating VDUs
        for (VirtualDeploymentUnit vdu_nfvo : virtualNetworkFunctionRecord_nfvo.getVdu()) {
            for (VirtualDeploymentUnit vdu_manager : virtualNetworkFunctionRecord.getVdu()) {
                //Found VDU -> Updaing
                if (vdu_nfvo.getId().equals(vdu_manager.getId())) {
                    vdu_nfvo.setVimInstance(vdu_manager.getVimInstance());
                    vdu_nfvo.setComputation_requirement(vdu_manager.getComputation_requirement());
                    vdu_nfvo.setHigh_availability(vdu_manager.getHigh_availability());
                    vdu_nfvo.setScale_in_out(vdu_manager.getScale_in_out());
                    vdu_nfvo.setVdu_constraint(vdu_manager.getVdu_constraint());
                    vdu_nfvo.setVirtual_memory_resource_element(vdu_manager.getVirtual_memory_resource_element());
                    vdu_nfvo.setVirtual_network_bandwidth_resource(vdu_manager.getVirtual_network_bandwidth_resource());
                    vdu_nfvo.setVm_image(vdu_manager.getVm_image());
                    //Updating VNFCInstances
                    vdu_nfvo.setVnfc_instance(updateVNFCInstances(vdu_nfvo.getVnfc_instance(), vdu_manager.getVnfc_instance()));
                }
            }
        }
        virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord_nfvo);
    }

    private Set<VNFCInstance> updateVNFCInstances(Set<VNFCInstance> vnfcInstances_nfvo, Set<VNFCInstance> vnfcInstances_manager) {
        Set<VNFCInstance> instances = new HashSet<>();
        boolean found = false;
        //Updating existing Instances, adding new ones and ignoring old ones
        for (VNFCInstance vnfcInstance_manager : vnfcInstances_manager) {
            //VNFCInstance ID is null -> NEW
            if (vnfcInstance_manager.getId() == null) {
                instances.add(vnfcInstance_manager);
                continue;
            }
            for (VNFCInstance vnfcInstance_nfvo : vnfcInstances_nfvo) {
                //Found Instance -> Updating
                if (vnfcInstance_nfvo.getId().equals(vnfcInstance_manager.getId())) {
                    found = true;
                    //Update Instance
                    vnfcInstance_nfvo.setHostname(vnfcInstance_manager.getHostname());
                    vnfcInstance_nfvo.setVim_id(vnfcInstance_manager.getVim_id());
                    vnfcInstance_nfvo.setVc_id(vnfcInstance_manager.getVc_id());
                    vnfcInstance_nfvo.setConnection_point(updateVNFDConnectionPoints(vnfcInstance_nfvo.getConnection_point(), vnfcInstance_manager.getConnection_point()));
                    //Add updated VNFCInstance
                    instances.add(vnfcInstance_nfvo);
                    //Proceed with the next VNFCInstance
                    break;
                }
            }
            //VNFCInstance was not found -> NEW
            if (!found) {
                instances.add(vnfcInstance_manager);
            }
        }
        return instances;
    }

    private Set<VNFDConnectionPoint> updateVNFDConnectionPoints(Set<VNFDConnectionPoint> vnfdConnectionPoints_nfvo, Set<VNFDConnectionPoint> vnfdConnectionPoints_manager) {
        Set<VNFDConnectionPoint> vnfdConnectionPoints = new HashSet<>();
        boolean found = false;
        //Updating existing VNFDConnectionPoints, adding new ones and ignoring old ones
        for (VNFDConnectionPoint vnfdConnectionPoint_manager : vnfdConnectionPoints_manager) {
            //VNFDConnectionPoint ID is null -> NEW
            if (vnfdConnectionPoint_manager.getId() == null) {
                vnfdConnectionPoints.add(vnfdConnectionPoint_manager);
                continue;
            }
            for (VNFDConnectionPoint vnfdConnectionPoint_nfvo : vnfdConnectionPoints_nfvo) {
                //Found VNFDConnectionPoint -> Updating
                if (vnfdConnectionPoint_nfvo.getId().equals(vnfdConnectionPoint_manager.getId())) {
                    found = true;
                    vnfdConnectionPoint_nfvo.setVirtual_link_reference(vnfdConnectionPoint_manager.getVirtual_link_reference());
                    vnfdConnectionPoint_nfvo.setExtId(vnfdConnectionPoint_manager.getExtId());
                    vnfdConnectionPoint_nfvo.setType(vnfdConnectionPoint_manager.getType());
                    vnfdConnectionPoint_nfvo.setName(vnfdConnectionPoint_manager.getName());
                    //Add updated VNFDConnectionPoint
                    vnfdConnectionPoints.add(vnfdConnectionPoint_nfvo);
                    //Proceed with the next VNFDConnectionPoint
                    break;
                }
            }
            //VNFDConnectionPoint not found -> NEW
            if (!found) {
                vnfdConnectionPoints.add(vnfdConnectionPoint_manager);
            }
        }
        return vnfdConnectionPoints;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
