package dev.southside.module.impl.misc.disabler.aca;

public class Aimstep {
   private static final double NO_MOVE_DELTA_THRESHOLD = 1.0E-5;
   private static final double STEP_MOVE_DELTA_THRESHOLD = 1.0;

   public static double yawDelta(double yaw1, double yaw2) {
      double diff = yaw1 - yaw2;
      return Math.abs(normalizeYaw(diff));
   }

   public static double normalizeYaw(double yaw) {
      return ((yaw + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
   }

   public static double pitchDelta(float a, float b) {
      return a > b ? a - b : b - a;
   }

   public static boolean isAimStep(double deltaOne, double deltaTwo) {
      return deltaOne < 1.0E-5 && deltaTwo > 1.0;
   }
}
