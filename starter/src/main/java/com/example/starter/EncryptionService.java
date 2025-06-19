//package com.example.starter;
//
//import javax.crypto.Cipher;
//import java.security.*;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class EncryptionService {
//  private final Map<String, KeyPair> map = new ConcurrentHashMap<>();
//
//  public PublicKey createSessionKeyPair(String sessionId) throws Exception {
//    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//    keyGen.initialize(2048);
//    KeyPair keyPair = keyGen.generateKeyPair();
//    map.put(sessionId, keyPair);
//    return keyPair.getPublic();
//  }
//
//  public String getEncodedData(String base64PublicKey, String valueToEncrypt) {
//    try {
//      PublicKey publicKey = convertBase64ToPublicKey(base64PublicKey);
//      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//      byte[] encryptedBytes = cipher.doFinal(valueToEncrypt.getBytes());
//      return Base64.getEncoder().encodeToString(encryptedBytes);
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
//
//  public String decryptedData(String encryptedMessage, String sessionId) {
//    try {
//      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//      PrivateKey privateKey = getPrivateKey(sessionId);
//      if (privateKey == null) return null;
//
//      cipher.init(Cipher.DECRYPT_MODE, privateKey);
//
//      // Replace space with + if data corrupted
//      byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage.replace(" ", "+"));
//      byte[] decryptedBytes = cipher.doFinal(decodedBytes);
//      return new String(decryptedBytes);
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
//
//  public PrivateKey getPrivateKey(String sessionId) {
//    KeyPair keyPair = map.get(sessionId);
//    return (keyPair != null) ? keyPair.getPrivate() : null;
//  }
//
//  public String convertPublicKeyToBase64(PublicKey publicKey) {
//    return Base64.getEncoder().encodeToString(publicKey.getEncoded());
//  }
//
//  private PublicKey convertBase64ToPublicKey(String base64PublicKey) {
//    try {
//      byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
//      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
//      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//      return keyFactory.generatePublic(keySpec);
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
//}
