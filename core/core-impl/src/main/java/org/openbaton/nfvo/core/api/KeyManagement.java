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

package org.openbaton.nfvo.core.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.KeyRepository;
import org.openbaton.utils.key.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

/** Created by lto on 13/05/15. */
@Service
@Scope
public class KeyManagement implements org.openbaton.nfvo.core.interfaces.KeyManagement {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private KeyRepository keyRepository;

  @Override
  public Iterable<Key> query(String projectId) {
    return keyRepository.findByProjectId(projectId);
  }

  @Override
  public Key queryById(String projectId, String id) throws NotFoundException {
    Key key = keyRepository.findFirstById(id);
    if (key == null) {
      throw new NotFoundException("Not found key with id " + id);
    }
    if (!key.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException("Forbidden to query this project");
    }
    return key;
  }

  @Override
  public Key queryByName(String projectId, String name) {
    return keyRepository.findKey(projectId, name);
  }

  @Override
  public void delete(String projectId, String id) throws NotFoundException {
    Key keyToDelete = keyRepository.findFirstById(id);
    if (keyToDelete == null) {
      throw new NotFoundException("Not found key with id " + id);
    }
    if (!keyToDelete.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException("Forbidden to delete this project");
    }
    keyRepository.delete(id);
  }

  @Override
  public String generateKey(String projectId, String name)
      throws IOException, NoSuchAlgorithmException {
    log.debug("Generating keypair");
    KeyPair keyPair = KeyHelper.generateRSAKey();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    String publicKeyString = KeyHelper.encodePublicKey(publicKey, name);
    Key key = new Key();
    key.setName(name);
    key.setProjectId(projectId);
    key.setFingerprint(KeyHelper.calculateFingerprint(publicKey.getEncoded()));
    key.setPublicKey(publicKeyString);
    log.debug(publicKeyString);
    keyRepository.save(key);
    log.info("Added new key: " + key);
    return KeyHelper.parsePrivateKey(privateKey.getEncoded());
  }

  @Override
  public Key addKey(String projectId, String name, String key)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    Key keyToAdd = new Key();
    keyToAdd.setName(name);
    keyToAdd.setProjectId(projectId);
    keyToAdd.setPublicKey(key);
    keyToAdd.setFingerprint(KeyHelper.calculateFingerprint(KeyHelper.parsePublicKey(key)));
    keyRepository.save(keyToAdd);
    return keyToAdd;
  }
}
