package org.openbaton.nfvo.core.core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
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

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lto on 17/05/16.
 */
@Service
public class LogManagement implements org.openbaton.nfvo.core.interfaces.LogManagement {

  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private Gson gson;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public List<String> getLog(String nsrId, String vnfrName, String hostname)
      throws NotFoundException {
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
              log.debug("RECEIVED: " + json);

              JsonReader reader = new JsonReader(new StringReader(json));
              reader.setLenient(true);
              JsonArray answer = null;
              try {
                answer =
                    ((JsonObject) gson.fromJson(reader, JsonObject.class))
                        .get("answer")
                        .getAsJsonArray();
              } catch (IllegalStateException ignored) {
                LinkedList<String> error = new LinkedList<>();
                error.add(
                    ((JsonObject) gson.fromJson(reader, JsonObject.class))
                        .get("answer")
                        .getAsString());
                return error;
              }
              log.debug("ANSWER: " + answer);
              return gson.fromJson(answer, LinkedList.class);
            }
          }
        }
      }
    }

    throw new NotFoundException("Error something was not found");
  }
}
