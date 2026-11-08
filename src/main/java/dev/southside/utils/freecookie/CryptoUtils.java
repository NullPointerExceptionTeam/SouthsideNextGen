package dev.southside.utils.freecookie;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

public class CryptoUtils {
   public static String clientSignature(String secretKey) {
      long timestamp = Instant.now().getEpochSecond();
      String tsString = Long.toString(timestamp);
      String raw = secretKey + tsString;
      String signature = computeSha256(raw);
      String combined = tsString + ":" + signature;
      return Base64.getEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
   }

   private static String computeSha256(String input) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
         StringBuilder hexString = new StringBuilder();

         for (byte b : hash) {
            hexString.append(String.format("%02x", b));
         }

         return hexString.toString();
      } catch (NoSuchAlgorithmException var8) {
         throw new RuntimeException("SHA-256 not supported", var8);
      }
   }
}
