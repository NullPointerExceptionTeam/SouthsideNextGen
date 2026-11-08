package dev.southside.utils.rotation;

public class MoveFixCalculation {
   private static final int[] DELTAS = new int[]{0, -45, 45, -90, 90, -180};

   public static double normalize(double deg) {
      return ((deg + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
   }

   public static double angularDistanceAbs(double a, double b) {
      return Math.abs(normalize(a - b));
   }

   public static MoveFixResult chooseBest(double requested, double current) {
      requested = normalize(requested);
      current = normalize(current);
      double bestErr = Double.POSITIVE_INFINITY;
      int bestDelta = 0;
      double bestAdjusted = current;

      for (int d : DELTAS) {
         double adjusted = normalize(current + d);
         double err = angularDistanceAbs(adjusted, requested);
         if (err < bestErr) {
            bestErr = err;
            bestDelta = d;
            bestAdjusted = adjusted;
         } else if (Math.abs(err - bestErr) < 1.0E-9 && Math.abs(d) < Math.abs(bestDelta)) {
            bestDelta = d;
            bestAdjusted = adjusted;
         }
      }

      return new MoveFixResult(bestAdjusted, bestDelta, bestErr);
   }
}
