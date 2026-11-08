package dev.southside.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {
   public static final SecureRandom random = new SecureRandom();

   public static int clamp(int value, int min, int max) {
      return Math.min(Math.max(value, min), max);
   }

   public static float clamp(float value, float min, float max) {
      return value < min ? min : Math.min(value, max);
   }

   public static double random(double min, double max) {
      return ThreadLocalRandom.current().nextDouble() * (max - min) + min;
   }

   public static double clamp(double value, double min, double max) {
      return value < min ? min : Math.min(value, max);
   }

   public static float clampAngle(float angle) {
      angle %= 360.0F;
      if (angle >= 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   public static float clampAngle(float angle, float min, float max) {
      angle = clampAngle(angle);
      return Math.max(min, Math.min(max, angle));
   }

   public static double randomDouble(double min, double max) {
      return min + (max - min) * random.nextDouble();
   }

   public static double round(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = new BigDecimal(value);
         bd = bd.setScale(places, RoundingMode.HALF_UP);
         return bd.doubleValue();
      }
   }

   public static double getRandomInRange(double min, double max) {
      SecureRandom random = new SecureRandom();
      return min == max ? min : random.nextDouble() * (max - min) + min;
   }

   public static float sqrt(float value) {
      return (float)Math.sqrt(value);
   }

   public static int getRandomInRange(int min, int max) {
      return (int)(Math.random() * (max - min) + min);
   }

   public static double wrapAngleTo180_double(double value) {
      value %= 360.0;
      if (value >= 180.0) {
         value -= 360.0;
      }

      if (value < -180.0) {
         value += 360.0;
      }

      return value;
   }

   public static float wrapAngleTo180_float(float value) {
      value %= 360.0F;
      if (value >= 180.0F) {
         value -= 360.0F;
      }

      if (value < -180.0F) {
         value += 360.0F;
      }

      return value;
   }

   public static double getDotProductYaw(float currentYaw, float lastYaw) {
      double radCurrent = Math.toRadians(currentYaw);
      double radLast = Math.toRadians(lastYaw);
      double x1 = Math.cos(radCurrent);
      double z1 = Math.sin(radCurrent);
      double x2 = Math.cos(radLast);
      double z2 = Math.sin(radLast);
      return x1 * x2 + z1 * z2;
   }

   public static boolean approxEqual(double a, double b) {
      double EPSILON = 1.0E-4;
      return Math.abs(a - b) < 1.0E-4;
   }

   public static boolean approxEqual(double a, float b) {
      return approxEqual(a, (double)b);
   }

   public static double interpolate(double min, double max, double rate) {
      return min + (max - min) * rate;
   }
}
