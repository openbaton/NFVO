/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.vim_impl.monitoring;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.monitoring.interfaces.VirtualisedResourcesPerformanceManagement;
import org.openbaton.nfvo.vim_interfaces.monitoring.MonitoringBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.rmi.RemoteException;

/**
 * Created by lto on 05/08/15.
 */
@Service
@Scope("prototype")
public class MonitoringManager {

    private VirtualisedResourcesPerformanceManagement virtualisedResourcesPerformanceManagement;

    @Autowired
    private MonitoringBroker monitoringBroker;

    @PostConstruct
    private void init(){
        //TODO using types
        this.virtualisedResourcesPerformanceManagement = monitoringBroker.getAvailableMonitoringAgent();
    }

    public Item getMeasurmentResults(VirtualDeploymentUnit virtualDeploymentUnit, String metric, String period){

//        return resourcePerformanceManagement.getMeasurementResults(virtualDeploymentUnit,metric,period);
        return null;
    }

    public void notifyResults() throws RemoteException, MonitoringException {
        //resourcePerformanceManagement.notifyResults();
    }
}
