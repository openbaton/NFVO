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

package org.project.openbaton.nfvo.vnfm_reg.impl.receiver;

import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.exceptions.*;
import org.project.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * Created by lto on 26/05/15.
 */
@RestController
@RequestMapping("/admin/v1/vnfm-core-actions")
public class VnfmReceiverRest implements VnfmReceiver {

    @Autowired
    private org.project.openbaton.vnfm.interfaces.manager.VnfmManager vnfmManager;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void actionFinished(@RequestBody NFVMessage nfvMessage,@Header(name= JmsHeaders.REPLY_TO, required = false) String tempDestination) throws NotFoundException, VimException {
        log.debug("CORE: Received: " + nfvMessage);
        vnfmManager.executeAction(nfvMessage, tempDestination);
    }

}
