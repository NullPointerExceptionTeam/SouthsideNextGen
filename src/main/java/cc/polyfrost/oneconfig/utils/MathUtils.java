package cc.polyfrost.oneconfig.utils;

public final class MathUtils {
   public static float clamp(float number) {
      return clamp(number, 0.0F, 1.0F);
   }

   public static float clamp(float number, float min, float max) {
      return number < min ? min : Math.min(number, max);
   }

   public static float map(float value, float start1, float stop1, float start2, float stop2) {
      return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
   }

   public static double mapIncrement(double value, double start1, double stop1, double start2, double stop2, double increment) {
      double rate = (value - start1) / (stop1 - start1);
      double rawValue = rate * (stop2 - start2);
      return Math.round((Math.round(rawValue / increment) * increment + start2) * 10000.0) / 10000.0;
   }
}
