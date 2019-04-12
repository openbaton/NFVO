/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.core.api;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.utils.key.KeyHelper;
import org.openbaton.nfvo.repositories.KeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
  public Key queryById(String projectId, String id) {
    return keyRepository.findFirstByIdAndProjectId(id, projectId);
  }

  @Override
  public Key queryByName(String projectId, String name) {
    return keyRepository.findKey(projectId, name);
  }

  @Override
  public void delete(String projectId, String id) throws NotFoundException {
    Key keyToDelete = keyRepository.findFirstByIdAndProjectId(id, projectId);
    if (keyToDelete == null) {
      throw new NotFoundException("Not found key with id " + id);
    }
    keyRepository.delete(id);
  }

  @Override
  public String generateKey(String projectId, String name)
      throws IOException, NoSuchAlgorithmException, AlreadyExistingException {
    log.debug("Generating keypair");
    if (keyRepository.findKey(projectId, name) != null)
      throw new AlreadyExistingException("A key with the name " + name + " exists already.");
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
      throws NoSuchAlgorithmException, BadFormatException, AlreadyExistingException {
    if (keyRepository.findKey(projectId, name) != null)
      throw new AlreadyExistingException("A key with the name " + name + " exists already.");
    Key keyToAdd = new Key();
    keyToAdd.setName(name);
    keyToAdd.setProjectId(projectId);
    keyToAdd.setPublicKey(key);
    keyToAdd.setFingerprint(KeyHelper.calculateFingerprint(KeyHelper.parsePublicKey(key)));
    keyRepository.save(keyToAdd);
    return keyToAdd;
  }
}
