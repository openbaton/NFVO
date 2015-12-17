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
