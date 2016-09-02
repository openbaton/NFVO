package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.NotFoundException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by lto on 10/03/16.
 */
public interface KeyManagement {

  Iterable<Key> query(String projectId);

  Key queryById(String projectId, String id) throws NotFoundException;

  Key queryByName(String projectId, String name);

  void delete(String projectId, String id) throws NotFoundException;

  String generateKey(String projectId, String name)
      throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException,
          IOException;

  Key addKey(String projectId, String name, String key)
      throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException;
}
