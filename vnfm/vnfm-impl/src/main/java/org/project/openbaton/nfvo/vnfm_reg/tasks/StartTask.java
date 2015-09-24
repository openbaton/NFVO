/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class StartTask extends AbstractTask {

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void doWork() throws Exception {
        log.debug("----> STARTED VNFR: " + virtualNetworkFunctionRecord.getName());
        log.debug("vnfr arrived version= " + virtualNetworkFunctionRecord.getHb_version());

        VirtualNetworkFunctionRecord existingvnfr = vnfrRepository.findOne(virtualNetworkFunctionRecord.getId());
        log.debug("vnfr existing version= " + existingvnfr.getHb_version());
        virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord);
    }
}
