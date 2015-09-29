package org.openbaton.nfvo.vim_interfaces.monitoring;

import org.openbaton.monitoring.interfaces.ResourcePerformanceManagement;

/**
 * Created by lto on 05/08/15.
 */
public interface MonitoringBroker {
    void addAgent(ResourcePerformanceManagement resourcePerformanceManagement, String type);

    ResourcePerformanceManagement getAvailableMonitoringAgent();
}
