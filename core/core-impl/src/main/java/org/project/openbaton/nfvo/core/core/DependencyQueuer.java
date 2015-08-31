package org.project.openbaton.nfvo.core.core;

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
import java.util.*;

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

    private Map<String, Set<String>> queues;
    @Autowired
    private NetworkServiceRecordManagement networkServiceRecordManagement;

    @PostConstruct
    private void init() {
        this.queues = new HashMap<>();
    }

    @Override
    public synchronized void waitForVNFR(String targetDependencyId, Set<String> sourceIds) throws InterruptedException, NotFoundException {
        if (queues.get(targetDependencyId) == null) {
            queues.put(targetDependencyId, new HashSet<String>());
        }
        log.debug("Adding to the queue: " + sourceIds + ", dependency: " + targetDependencyId);
        for (String name : sourceIds)
            queues.get(targetDependencyId).add(name);
    }

    @Override
    public synchronized void releaseVNFR(String vnfrSourceId) throws NotFoundException {
        List<String> dependencyIdToBeRemoved = new ArrayList<>();
        log.debug("Doing release for VNFR id: " + vnfrSourceId);
        for (Map.Entry<String, Set<String>> entry : queues.entrySet()) {
            String dependencyId = entry.getKey();
            Set<String> sourceList = entry.getValue();
            log.debug("Dependency " + dependencyId + " contains " + sourceList.size() + " dependencies: " + sourceList);
            if (sourceList.contains(vnfrSourceId)) {
                sourceList.remove(vnfrSourceId);
                if (sourceList.size() == 0) {
                    CoreMessage coreMessage = new CoreMessage();
                    coreMessage.setAction(Action.MODIFY);
                    VNFRecordDependency vnfRecordDependency = vnfrDependencyRepository.find(dependencyId);
                    coreMessage.setDependency(vnfRecordDependency);
                    VirtualNetworkFunctionRecord target = vnfrRepository.find(vnfRecordDependency.getTarget().getId());
                    coreMessage.setVirtualNetworkFunctionRecord(target);
                    vnfmManager.modify(target, coreMessage);
                    dependencyIdToBeRemoved.add(dependencyId);
                }
            }
        }

        for (String depIdToRem : dependencyIdToBeRemoved){
            for (String depId: queues.keySet()) {
                if (depIdToRem.equals(depId)) {
                    queues.remove(depId);
                    break;
                }
            }
        }
    }
}
