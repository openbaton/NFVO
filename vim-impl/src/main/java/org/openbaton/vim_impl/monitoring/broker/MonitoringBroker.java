package org.openbaton.vim_impl.monitoring.broker;

import org.openbaton.monitoring.interfaces.VirtualisedResourcesPerformanceManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * Created by lto on 05/08/15.
 */
@Service
@Scope
public class MonitoringBroker implements org.openbaton.nfvo.vim_interfaces.monitoring.MonitoringBroker {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private HashMap<String, VirtualisedResourcesPerformanceManagement> VirtualisedResourcesPerformanceManagements;

    @PostConstruct
    private void init(){
        this.VirtualisedResourcesPerformanceManagements = new HashMap<>();
    }

    @Override
    public void addAgent(VirtualisedResourcesPerformanceManagement virtualisedResourcesPerformanceManagement, String type){
        log.info("Registered monitoring pluging of type: " + type);
        this.VirtualisedResourcesPerformanceManagements.put(type, virtualisedResourcesPerformanceManagement);
    }

    @Override
    public VirtualisedResourcesPerformanceManagement getAvailableMonitoringAgent() {
        return VirtualisedResourcesPerformanceManagements.values().iterator().next();
    }
}
