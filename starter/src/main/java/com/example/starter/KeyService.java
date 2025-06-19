package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyService {
  public PublicKey createSessionKeyPair(String sessionId) throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048); // Key size
    KeyPair keyPair = keyGen.generateKeyPair();
    PrivateKey privateKey = keyPair.getPrivate();
    String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

    RedisConnectionVerticle.getRedisConnection().send(
      Request.cmd(Command.SET)
        .arg("privateKey:" + sessionId)
        .arg(privateKeyBase64));
    return keyPair.getPublic();
  }

  public String getEncodedData(String base64PublicKey, String valueToEncrypt) {
    try {
      PublicKey publicKey = convertBase64ToPublicKey(base64PublicKey);

      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      byte[] encryptedBytes = cipher.doFinal(valueToEncrypt.getBytes());
      String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);
//      System.out.println("Encrypted Message: " + encryptedMessage);
      return encryptedMessage;

    } catch (Exception e) {
      System.out.println(e);
      return null;
    }
  }

  public Future<String> decryptedData(String encryptedMessage, String sessionId) {
    return getPrivateKey(sessionId).compose(privateKey -> {
      try {
//        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        cipher.init(Cipher.DECRYPT_MODE, privateKey);
//        String cleaned = encryptedMessage.replaceAll(" ", "+");
//        System.out.println("Cleaned Encrypted: " + cleaned);
//        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cleaned));
//        String decryptedMessage = new String(decryptedBytes);
////        System.out.println("Decrypted Message: " + decryptedMessage);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
          "SHA-256",
          "MGF1",
          MGF1ParameterSpec.SHA256,
          PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage.replaceAll(" ","+"));
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        String decrypted = new String(decryptedBytes);
//        System.out.println("Decrypted message: " + decrypted);
        return Future.succeededFuture(decrypted);
      } catch (Exception e) {
        e.printStackTrace();
        return Future.failedFuture(e);
      }
    });
  }

  public Future<PrivateKey> getPrivateKey(String sessionId) {
    Promise<PrivateKey> promise = Promise.promise();

    RedisConnectionVerticle.getRedisConnection().send(
      Request.cmd(Command.GET).arg("privateKey:" + sessionId), redisResult -> {
        if (redisResult.succeeded() && redisResult.result() != null) {
          try {
            byte[] decodedKey = Base64.getDecoder().decode(redisResult.result().toBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            promise.complete(key);
          } catch (Exception e) {
            promise.fail(e);
          }
        } else {
          promise.fail("Key not found or Redis error");
        }
      }
    );

    return promise.future();
  }

//  public void invalidateSession(String sessionId) {
//    sessionKeyStore.remove(sessionId);
//  }

  public String convertPublicKeyToBase64(PublicKey publicKey) {
    return Base64.getEncoder().encodeToString(publicKey.getEncoded());
  }

  private PublicKey convertBase64ToPublicKey(String publicKey) {
    try {
      byte[] decodedKey = Base64.getDecoder().decode(publicKey);
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(keySpec);
    } catch (Exception e) {
      System.out.println(e);
      return null;
    }

  }
}
