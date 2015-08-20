package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lto on 19/08/15.
 */
@Service
@Scope
public class DependencyQueuer implements org.project.openbaton.nfvo.core.interfaces.DependencyQueuer {
    @Autowired
    @Qualifier("vnfmManager")
    private VnfmManager vnfmManager;
    @Autowired
    @Qualifier("VNFRRepository")
    private GenericRepository<VirtualNetworkFunctionRecord> vnfrRepository;

    @Autowired
    @Qualifier("VNFRDependencyRepository")
    private GenericRepository<VNFRecordDependency> vnfrDependencyRepository;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, BlockingDeque<VNFRecordDependency>> queues;
    @Autowired
    private NetworkServiceRecordManagement networkServiceRecordManagement;

    @PostConstruct
    private void init(){
        this.queues = new HashMap<>();
    }

    @Override
    public synchronized void waitForVNFR(String vnfrTargetId, VNFRecordDependency dependency) throws InterruptedException, NotFoundException {
        if (queues.get(vnfrTargetId) == null){
            queues.put(vnfrTargetId, new LinkedBlockingDeque<VNFRecordDependency>());
        }
        log.debug("Adding to the queue: " + vnfrTargetId + ", dependency: " + dependency);
        queues.get(vnfrTargetId).add(dependency);
    }

    @Override
    public synchronized void releaseVNFR(String vnfrSourceId) throws NotFoundException {
        log.debug("Doing release for VNFR id: " + vnfrSourceId);
        for (BlockingDeque<VNFRecordDependency> qs : queues.values()) {
            List<VNFRecordDependency> dependenciesToBeRemoved = new ArrayList<>();
            for (VNFRecordDependency dependency: qs) {
                if (dependency.getSource().getId().equals(vnfrSourceId)) {
                    CoreMessage coreMessage = new CoreMessage();
                    coreMessage.setAction(Action.MODIFY);
                    coreMessage.setDependency(vnfrDependencyRepository.find(dependency.getId()));
                    VirtualNetworkFunctionRecord target = vnfrRepository.find(dependency.getTarget().getId());
                    coreMessage.setVirtualNetworkFunctionRecord(target);
                    vnfmManager.modify(target, coreMessage);
                    dependenciesToBeRemoved.add(dependency);
                }
            }
            for (VNFRecordDependency dependencyToRemove : dependenciesToBeRemoved){
                for (VNFRecordDependency dependency : qs){
                    if (dependency.getId().equals(dependencyToRemove.getId())){
                        qs.remove(dependency);
                        break;
                    }
                }
            }
        }
//                element.setWaitingFor(element.getWaitingFor() - 1);
//                log.debug("releasing from the queue: " + vnfrSourceId + ", dependency: " + element.getDependencies() + " that is waiting for: " + element.getWaitingFor());
//                if (element.getWaitingFor() == 0) {
//                    for (VNFRecordDependency dependency : element.getDependencies()) {
//                        log.trace("");
//                        log.trace("");
//                        log.trace("Sending modify to " + dependency.getTarget().getName() + " ( " + dependency.getTarget().getId() + " )");
//                        log.trace("");
//                        log.trace("");
//
//                    }
//                }
//            }
//        }
    }

    @Override
    public synchronized boolean resolvedDependencies(String networkServiceId){
        NetworkServiceRecord networkServiceRecord = networkServiceRecordManagement.query(networkServiceId);

        for (VNFRecordDependency dependency : networkServiceRecord.getVnf_dependency()){
            if (vnfrDependencyRepository.find(dependency.getId()).getStatus().ordinal() != Status.ACTIVE.ordinal())
                return false;
        }

        return true;
    }

    @Override
    public synchronized int calculateDependencies(String virtualNetworkFunctionRecordId) {
        try {
            int dep = queues.get(virtualNetworkFunctionRecordId).size();
            log.debug("Calculating dependencies for: " + virtualNetworkFunctionRecordId + " == " + dep);
            return dep;
        }catch (Exception e){
//            e.printStackTrace();
//            throw e;
            return 0;
        }
    }
}
