package org.openbaton.nfvo.core.interfaces;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.openbaton.catalogue.security.ServiceMetadata;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;

/** Created by lto on 04/04/2017. */
public interface ComponentManager {

  String registerService(String body)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException, NotFoundException;

  String createService(String serviceName, String projectId, List<String> projects)
      throws NoSuchAlgorithmException, IOException, NotFoundException, MissingParameterException;

  String enableManager(String message) throws IOException;

  boolean isService(String token)
      throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
          IllegalBlockSizeException, NoSuchPaddingException;

  Iterable<ServiceMetadata> listServices();

  void removeService(String id);

  void removeTokens();
}
