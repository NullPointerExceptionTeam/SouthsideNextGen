package dev.southside.utils.math;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
   private static Random RAND = new Random();
   private static Random BACKUP = new Random();

   public static void backup() {
      BACKUP = RAND;
   }

   public static void restore() {
      RAND = BACKUP;
   }

   public static void setSeed(long seed) {
      RAND = new Random(seed);
   }

   public static double random(double min, double max) {
      return RAND.nextDouble() * (max - min) + min;
   }

   public static float random(float min, float max) {
      return (float)(RAND.nextDouble() * (max - min) + min);
   }

   public static long randomDelay(int minDelay, int maxDelay) {
      return nextInt(minDelay, maxDelay);
   }

   public static long nextLong(long origin, long bound) {
      return origin == bound ? origin : ThreadLocalRandom.current().nextLong(origin, bound);
   }

   public static int nextInt(int startInclusive, int endExclusive) {
      return endExclusive - startInclusive <= 0 ? startInclusive : startInclusive + RAND.nextInt(1, endExclusive - startInclusive);
   }

   public static double nextDouble(double startInclusive, double endInclusive) {
      return startInclusive != endInclusive && !(endInclusive - startInclusive <= 0.0)
         ? startInclusive + (endInclusive - startInclusive) * RAND.nextDouble()
         : startInclusive;
   }

   public static float nextFloat(double startInclusive, double endInclusive) {
      return (float)(
         startInclusive != endInclusive && !(endInclusive - startInclusive <= 0.0)
            ? startInclusive + (endInclusive - startInclusive) * RAND.nextDouble()
            : startInclusive
      );
   }

   public static boolean nextBoolean() {
      return RAND.nextBoolean();
   }

   public static String randomNumber(int length) {
      return random(length, "123456789");
   }

   public static String randomString(int length) {
      return random(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
   }

   public static String randomStringA(int length) {
      return random(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
   }

   public static String randomStringHex(int length) {
      return random(length, "1234567890ABCDEF");
   }

   public static String randomStringLower(int length) {
      return random(length, "0123456789abcdefghijklmnopqrstuvwxyz");
   }

   public static String randomStringHexLower(int length) {
      return random(length, "0123456789abcdef");
   }

   public static String random(int length, String chars) {
      return random(length, chars.toCharArray());
   }

   public static String random(int length, char[] chars) {
      StringBuilder stringBuilder = new StringBuilder();

      for (int i = 0; i < length; i++) {
         stringBuilder.append(chars[RAND.nextInt(chars.length)]);
      }

      return stringBuilder.toString();
   }

   public static void nextBytes(byte[] array) {
      RAND.nextBytes(array);
   }

   public static <T> T nextArray(T[] array) {
      return array.length == 0 ? null : array[RAND.nextInt(array.length)];
   }

   public static <T> T nextList(List<T> list) {
      return list.isEmpty() ? null : list.get(RAND.nextInt(list.size()));
   }

   public static float generateRandomFloat(float minPrecision, float maxPrecision) {
      if (!(minPrecision <= 0.0F) && !(maxPrecision <= minPrecision)) {
         boolean negativeRange = RAND.nextBoolean();
         if (negativeRange) {
            return -minPrecision - RAND.nextFloat() * (maxPrecision - minPrecision);
         } else {
            float result;
            do {
               result = -minPrecision + RAND.nextFloat() * (maxPrecision + minPrecision);
            } while (Math.abs(result) < minPrecision);

            return result;
         }
      } else {
         throw new IllegalArgumentException("minPrecision must be > 0 and maxPrecision > minPrecision");
      }
   }
}
