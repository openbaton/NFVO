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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.project.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.project.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.exceptions.NotFoundException;
import org.project.openbaton.exceptions.VimException;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.project.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.project.openbaton.nfvo.repositories.VNFRRepository;
import org.project.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

import javax.jms.Destination;
import java.util.concurrent.ExecutionException;


/**
 * Created by lto on 26/05/15.
 */
@RestController
@RequestMapping("/admin/v1/")
public class VnfmReceiverRest implements VnfmReceiver {

    @Autowired
    private NetworkServiceRecordRepository networkServiceRecordRepository;

    private Gson mapper = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private VNFRRepository vnfrRepository;

    @Autowired
    private VNFLifecycleOperationGranting vnfLifecycleOperationGranting;

    @Autowired
    private ResourceManagement resourceManagement;

    @Autowired
    private org.project.openbaton.vnfm.interfaces.manager.VnfmManager vnfmManager;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @RequestMapping(value = "vnfm-core-actions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void actionFinished(@RequestBody Object nfvMessage, @Header(name = JmsHeaders.REPLY_TO, required = false) Destination tempDestination) throws NotFoundException, VimException {
        log.debug("CORE: Received: " + nfvMessage);
        String  action = mapper.fromJson(mapper.toJson(nfvMessage), JsonObject.class).get("action").getAsString();
        NFVMessage message;
        if (action.equals("INSTANTIATE")) {
            message = mapper.fromJson(mapper.toJson(nfvMessage), VnfmOrInstantiateMessage.class);
            log.trace("DESERIALIZED: " + message);
        }
        else{
            message = mapper.fromJson(mapper.toJson(nfvMessage), VnfmOrGenericMessage.class);
            log.trace("DESERIALIZED: " + message);
        }
        vnfmManager.executeAction(message, tempDestination);
    }

    @RequestMapping(value = "vnfm-core-grant", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NFVMessage grantLifecycleOperation(@RequestBody VnfmOrGenericMessage message) throws VimException {

        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = message.getVirtualNetworkFunctionRecord();
        if (vnfLifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord)) {
            return new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.GRANT_OPERATION);
        } else {
            return new OrVnfmErrorMessage(saveVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord), "Not enough resources");
        }
    }

    @RequestMapping(value = "vnfm-core-allocate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NFVMessage allocate(@RequestBody VnfmOrGenericMessage message) throws VimException {

        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = message.getVirtualNetworkFunctionRecord();
        try {
            for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu())
                resourceManagement.allocate(virtualDeploymentUnit, virtualNetworkFunctionRecord);

            for (LifecycleEvent event : virtualNetworkFunctionRecord.getLifecycle_event()) {
                if (event.getEvent().ordinal() == Event.ALLOCATE.ordinal()) {
                    virtualNetworkFunctionRecord.getLifecycle_event_history().add(event);
                    break;
                }
            }
            return new OrVnfmGenericMessage(saveVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord), Action.ALLOCATE_RESOURCES);
        } catch (InterruptedException e) {
            return new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
        } catch (ExecutionException e) {
            return new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
        } catch (VimDriverException e) {
            return new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
        }
    }

    private VirtualNetworkFunctionRecord saveVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        if (virtualNetworkFunctionRecord.getId() == null)
            return networkServiceRecordRepository.addVnfr(virtualNetworkFunctionRecord, virtualNetworkFunctionRecord.getParent_ns_id());
        else
            return vnfrRepository.save(virtualNetworkFunctionRecord);
    }
}
