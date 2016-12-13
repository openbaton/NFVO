/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
 *
 */

package org.openbaton.nfvo.core.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Created by lto on 17/05/16. */
@Service
public class LogManagement implements org.openbaton.nfvo.core.interfaces.LogManagement {

  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private Gson gson;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public HashMap getLog(String nsrId, String vnfrName, String hostname) throws NotFoundException {
    for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord :
        networkServiceRecordRepository.findFirstById(nsrId).getVnfr()) {
      if (virtualNetworkFunctionRecord.getName().equals(vnfrName)) {
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
          for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
            if (hostname.equals(vnfcInstance.getHostname())) {

              log.debug("Requesting log from GenericVNFM");
              String json =
                  (String)
                      rabbitTemplate.convertSendAndReceive(
                          "nfvo.vnfm.logs",
                          "{\"vnfrName\":\"" + vnfrName + "\", \"hostname\":\"" + hostname + "\"}");
              log.trace("RECEIVED: " + json);

              JsonReader reader = new JsonReader(new StringReader(json));
              reader.setLenient(true);
              JsonObject answer = null;
              try {
                answer =
                    ((JsonObject) gson.fromJson(reader, JsonObject.class))
                        .get("answer")
                        .getAsJsonObject();
              } catch (IllegalStateException e) {
                LinkedList<String> error = new LinkedList<>();
                error.add(
                    ((JsonObject) gson.fromJson(reader, JsonObject.class))
                        .get("answer")
                        .getAsString());
                for (String line : error) {
                  log.error(line);
                }
                throw e;
              }
              log.trace("ANSWER: " + answer);
              return gson.fromJson(answer, HashMap.class);
            }
          }
        }
      }
    }

    throw new NotFoundException("Error something was not found");
  }
}
