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

package org.openbaton.nfvo.vnfm_reg.impl.receiver;

import com.google.gson.Gson;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.VimException;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * Created by lto on 26/05/15.
 */
@Service
public class RabbitVnfmReceiver implements VnfmReceiver {

    @Autowired
    private Gson gson;

    @Autowired
    private VnfmManager vnfmManager;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public String actionFinished(String nfvMessage) throws NotFoundException, VimException, ExecutionException, InterruptedException {
        NFVMessage message = gson.fromJson(nfvMessage, NFVMessage.class);
        log.debug("CORE: Received: " + message.getAction());

        log.debug("----------Executing ACTION: " + message.getAction());
        String res = vnfmManager.executeAction(message);
        log.debug("-----------Finished ACTION: " + message.getAction());
        return res;
    }

    @Override
    public void actionFinishedVoid(String nfvMessage) throws NotFoundException, VimException, ExecutionException, InterruptedException {
        NFVMessage message = gson.fromJson(nfvMessage, NFVMessage.class);
        log.debug("CORE: Received: " + message.getAction());

        log.debug("----------Executing ACTION: " + message.getAction());
        vnfmManager.executeAction(message);
        log.debug("-----------Finished ACTION: " + message.getAction());
    }
}
