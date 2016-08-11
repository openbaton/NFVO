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

package org.openbaton.nfvo.core.api;

import org.apache.commons.codec.binary.Base64;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.KeyRepository;
import org.openbaton.nfvo.security.interfaces.ProjectManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope
public class KeyManagement implements org.openbaton.nfvo.core.interfaces.KeyManagement {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ProjectManagement projectManagement;

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
      throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException,
          IOException {
    log.debug("Generating keypair");
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.genKeyPair();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    String publicKeyString = encodePublicKey(publicKey, name);
    Key key = new Key();
    key.setName(name);
    key.setProjectId(projectId);
    key.setPublicKey(publicKeyString);
    log.debug(publicKeyString);
    keyRepository.save(key);
    log.info("Added new key: " + key);
    //Project project = projectManagement.query(projectId);
    //    project.getKeys().add(key);
    //projectManagement.update(project);
    return parsePrivateKey(privateKey.getEncoded());
  }

  @Override
  public Key addKey(String projectId, String name, String key) {
    Key keyToAdd = new Key();
    keyToAdd.setName(name);
    keyToAdd.setProjectId(projectId);
    keyToAdd.setPublicKey(key);
    keyRepository.save(keyToAdd);
    return keyToAdd;
  }

  private String parsePrivateKey(byte[] encodedKey) {
    StringBuilder sb = new StringBuilder();
    sb.append("-----BEGIN RSA PRIVATE KEY-----\n");
    sb.append((new String(Base64.encodeBase64(encodedKey))).replaceAll("(.{72})", "$1\n"));
    sb.append("\n");
    sb.append("-----END RSA PRIVATE KEY-----\n");
    return sb.toString();
  }

  public String encodePublicKey(RSAPublicKey key, String keyname) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    /* encode the "ssh-rsa" string */
    byte[] sshrsa = new byte[] {0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's', 'a'};
    out.write(sshrsa);
    /* Encode the public exponent */
    BigInteger e = key.getPublicExponent();
    byte[] data = e.toByteArray();
    encodeUInt32(data.length, out);
    out.write(data);
    /* Encode the modulus */
    BigInteger m = key.getModulus();
    data = m.toByteArray();
    encodeUInt32(data.length, out);
    out.write(data);
    return "ssh-rsa " + Base64.encodeBase64String(out.toByteArray()) + " " + keyname;
  }

  public void encodeUInt32(int value, OutputStream out) throws IOException {
    byte[] tmp = new byte[4];
    tmp[0] = (byte) ((value >>> 24) & 0xff);
    tmp[1] = (byte) ((value >>> 16) & 0xff);
    tmp[2] = (byte) ((value >>> 8) & 0xff);
    tmp[3] = (byte) (value & 0xff);
    out.write(tmp);
  }

  private String encodePublicKey(PublicKey publicKey, String user) throws IOException {
    String publicKeyEncoded;
    RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
    ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(byteOs);
    dos.writeInt("ssh-rsa".getBytes().length);
    dos.write("ssh-rsa".getBytes());
    dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
    dos.write(rsaPublicKey.getPublicExponent().toByteArray());
    dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
    dos.write(rsaPublicKey.getModulus().toByteArray());
    publicKeyEncoded = new String(encodeBase64(byteOs.toByteArray()));
    return "ssh-rsa " + publicKeyEncoded + " " + user;
  }

  public static void main(String[] args)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException,
          IOException {
    KeyManagement keyManagement = new KeyManagement();
    System.out.println(keyManagement.generateKey("projectID", "test"));
  }
}
