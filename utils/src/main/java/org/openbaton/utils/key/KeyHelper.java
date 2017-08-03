package org.openbaton.utils.key;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

/** Created by lto on 04/04/2017. */
public class KeyHelper {

  private static final Logger log = LoggerFactory.getLogger(KeyHelper.class);

  public static String DESEDE_ALGORITHM = "DESede";
  private static String AES_ALGORITHM = "AES";

  public static String genKey() throws NoSuchAlgorithmException, IOException {
    // Using Apache Commons RNG for randomness
    Random rng = new Random();
    // Generates a 20 code point string, using only the letters a-z
    RandomStringGenerator generator =
        new RandomStringGenerator.Builder().withinRange('A', 'z').build();
    return generator.generate(16);
  }

  private static Key restoreKey(byte[] keyBytes) {
    return new SecretKeySpec(keyBytes, AES_ALGORITHM);
  }

  private static byte[] encrypt(byte[] bytes, Key key)
      throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException,
          NoSuchPaddingException, NoSuchAlgorithmException {

    Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    return cipher.doFinal(bytes);
  }

  private static byte[] encrypt(String text, Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {

    return encrypt(text.getBytes(StandardCharsets.UTF_8), key);
  }

  public static String encryptNew(String valueToEnc, String keyValue)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {
    Key key = generateKey(keyValue.getBytes());
    Cipher c = Cipher.getInstance(AES_ALGORITHM);
    c.init(Cipher.ENCRYPT_MODE, key);
    byte[] encValue = c.doFinal(valueToEnc.getBytes());
    return Base64.encodeBase64String(encValue);
  }

  public static String decryptNew(String encryptedValue, String keyValue)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException,
          BadPaddingException, IllegalBlockSizeException {
    Key key = generateKey(keyValue.getBytes());
    Cipher c = Cipher.getInstance(AES_ALGORITHM);
    c.init(Cipher.DECRYPT_MODE, key);
    byte[] decodedValue = decodeBase64(encryptedValue);
    byte[] decValue = c.doFinal(decodedValue);
    return new String(decValue);
  }

  private static Key generateKey(byte[] keyValue) {
    Key key = new SecretKeySpec(keyValue, AES_ALGORITHM);
    return key;
  }

  private static String decrypt(byte[] bytes, Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
  }

  private static String decrypt(String bytes, Key key)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException {

    return decrypt(bytes.getBytes(StandardCharsets.ISO_8859_1), key);
  }

  private static String encryptToString(String text, Key key)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {
    return new String(encrypt(text, key), StandardCharsets.ISO_8859_1);
  }

  /*
   *
   * RSA
   *
   */

  public static String encodePublicKey(PublicKey publicKey, String user) throws IOException {
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

  public static String calculateFingerprint(byte[] publicKey) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA1");

    String start = Hex.encodeHexString(digest.digest(publicKey));
    String res = new String();
    for (int i = 0; i < start.length(); i++) {
      if (i != 0 && i % 2 == 0) {
        res += ":";
      }
      res += start.charAt(i);
    }
    log.debug(res);
    return res;
  }

  public static byte[] parsePublicKey(String decodedKey) throws UnsupportedEncodingException {
    decodedKey = decodedKey.split(" ")[1];
    return Base64.decodeBase64(decodedKey.getBytes("utf-8"));
  }

  public static String parsePrivateKey(byte[] encodedKey) {
    StringBuilder sb = new StringBuilder();
    sb.append("-----BEGIN RSA PRIVATE KEY-----\n");
    sb.append((new String(Base64.encodeBase64(encodedKey))).replaceAll("(.{72})", "$1\n"));
    sb.append("\n");
    sb.append("-----END RSA PRIVATE KEY-----\n");
    return sb.toString();
  }

  private String encodePublicKey(RSAPublicKey key, String keyname) throws IOException {
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

  private void encodeUInt32(int value, OutputStream out) throws IOException {
    byte[] tmp = new byte[4];
    tmp[0] = (byte) ((value >>> 24) & 0xff);
    tmp[1] = (byte) ((value >>> 16) & 0xff);
    tmp[2] = (byte) ((value >>> 8) & 0xff);
    tmp[3] = (byte) (value & 0xff);
    out.write(tmp);
  }

  public static KeyPair generateRSAKey() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.genKeyPair();
    return keyPair;
  }

  public static void main(String[] args) throws Exception {
    String key = genKey();
    System.out.println("Key is: " + key);
    String message = "long message to be encrypted";
    System.out.println("Value clean: " + message);
    String res = KeyHelper.encryptNew(message, key);
    System.out.println("Value encrypted: " + res);
    message = KeyHelper.decryptNew(res, key);
    System.out.println("Value cleat: " + message);
  }
}
