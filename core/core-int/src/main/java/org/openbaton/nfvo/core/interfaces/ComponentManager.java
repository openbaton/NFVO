package org.openbaton.nfvo.core.interfaces;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.openbaton.catalogue.security.ServiceMetadata;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;

public interface ComponentManager {

  String registerService(String body)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException, NotFoundException;

  String createService(String serviceName, String projectId, List<String> projects)
      throws NotFoundException, MissingParameterException;

  String enableManager(String message);

  boolean isService(String token);

  Iterable<ServiceMetadata> listServices();

  void removeService(String id);

  void removeTokens();
}
