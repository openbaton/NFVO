package org.openbaton.utils.key;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

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
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 04/04/2017. */
public class KeyHelper {

  private static final Logger log = LoggerFactory.getLogger(KeyHelper.class);

  public static String DESEDE_ALGORITHM = "DESede";
  public static String AES_ALGORITHM = "AES";

  public static Key genKey() throws NoSuchAlgorithmException, IOException {
    KeyGenerator generator = KeyGenerator.getInstance(AES_ALGORITHM);
    generator.init(256);
    return generator.generateKey();
  }

  public static Key restoreKey(byte[] keyBytes) {
    return new SecretKeySpec(keyBytes, AES_ALGORITHM);
  }

  private static byte[] encrypt(byte[] bytes, Key key)
      throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException,
          NoSuchPaddingException, NoSuchAlgorithmException {

    Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    return cipher.doFinal(bytes);
  }

  public static byte[] encrypt(String text, Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {

    return encrypt(text.getBytes(StandardCharsets.UTF_8), key);
  }

  public static String decrypt(byte[] bytes, Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
  }

  public static String decrypt(String bytes, Key key)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException {

    return decrypt(bytes.getBytes(StandardCharsets.ISO_8859_1), key);
  }

  public static String encryptToString(String text, Key key)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
          BadPaddingException, IllegalBlockSizeException {
    return new String(encrypt(text, key), StandardCharsets.ISO_8859_1);
  }

  public static void main(String[] args)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException,
          IOException {
    //    KeyHelper.parsePublicKey(
    //        "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCMK" +
    //
    // "/Jp56ykfSNoEQalX9HYS2VqM9cQwcBvhmryp6I5PIYpJ7b2cRexygKrFNG994Xg7WghnOHIuxR89Z5kSOilaVx91Wid1Ez7ftNpFvyxv8CJ59Ry5R/OQ3zAFlsWdov+QJh5s7zlbh3P1m2PbhrJ0Q7Qe7J4uTXwXDT2K0a9EOHSc9iZ5dWGTAxz0LtyWEDdLHwSjmg4LvQwmWsFs9P+k0WTJH3efYvTmsBHmo3n4XiPKUIpoO3MQycFedOkGvo/LlRlktp9mdz+HIZBJL3tLzUcRERUOVsUwlFPWGdYp0Urpvb6gSMkOFFAb1LwZU3xeD0oN7qlsa1xZaTbU6a1 test");
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
}
