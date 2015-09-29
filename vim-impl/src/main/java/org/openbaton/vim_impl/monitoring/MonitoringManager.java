package org.openbaton.vim_impl.monitoring;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.monitoring.interfaces.ResourcePerformanceManagement;
import org.openbaton.nfvo.vim_interfaces.monitoring.MonitoringBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by lto on 05/08/15.
 */
@Service
@Scope("prototype")
public class MonitoringManager {

    private ResourcePerformanceManagement resourcePerformanceManagement;

    @Autowired
    private MonitoringBroker monitoringBroker;

    @PostConstruct
    private void init(){
        //TODO using types
        this.resourcePerformanceManagement = monitoringBroker.getAvailableMonitoringAgent();
    }

    public Item getMeasurmentResults(VirtualDeploymentUnit virtualDeploymentUnit, String metric, String period){

//        return resourcePerformanceManagement.getMeasurementResults(virtualDeploymentUnit,metric,period);
        return null;
    }

    public void notifyResults(){
        resourcePerformanceManagement.notifyResults();
    }
}
