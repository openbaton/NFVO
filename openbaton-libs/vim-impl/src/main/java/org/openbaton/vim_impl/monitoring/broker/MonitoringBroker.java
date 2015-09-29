package org.openbaton.vim_impl.monitoring.broker;

import org.openbaton.monitoring.interfaces.ResourcePerformanceManagement;
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

    private HashMap<String, ResourcePerformanceManagement> resourcePerformanceManagements;

    @PostConstruct
    private void init(){
        this.resourcePerformanceManagements = new HashMap<>();
    }

    @Override
    public void addAgent(ResourcePerformanceManagement resourcePerformanceManagement, String type){
        log.info("Registered monitoring pluging of type: " + type);
        this.resourcePerformanceManagements.put(type, resourcePerformanceManagement);
    }

    @Override
    public ResourcePerformanceManagement getAvailableMonitoringAgent() {
        return resourcePerformanceManagements.values().iterator().next();
    }
}
