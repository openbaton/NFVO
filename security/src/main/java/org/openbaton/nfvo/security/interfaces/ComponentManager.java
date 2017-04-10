package org.openbaton.nfvo.security.interfaces;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.openbaton.catalogue.nfvo.ServiceCredentials;

/** Created by lto on 04/04/2017. */
public interface ComponentManager {

  ServiceCredentials registerService(String body);

  byte[] enableService(JsonObject jsonObject, String projectId)
      throws NoSuchAlgorithmException, IOException;

  ManagerCredentials enableManager(JsonObject body) throws IOException;
}
