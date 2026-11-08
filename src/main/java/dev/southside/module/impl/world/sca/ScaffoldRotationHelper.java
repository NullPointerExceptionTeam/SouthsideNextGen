package dev.southside.module.impl.world.sca;

import dev.southside.utils.types.Rotation;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;

public class ScaffoldRotationHelper {
   private static final class_310 mc = class_310.method_1551();
   private static final float SMOOTH_FACTOR = 0.15F;
   private static final float MIN_ANGLE_DIFF = 0.1F;
   private static Rotation targetRotation = null;
   private static Rotation currentRotation = null;
   private static boolean isRotating = false;

   public static Rotation getRotation(class_2338 pos, class_2350 face) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         class_243 targetPos = class_243.method_24953(pos).method_1031(face.method_10148() * 0.501, face.method_10164() * 0.501, face.method_10165() * 0.501);
         class_243 playerPos = mc.field_1724.method_33571();
         return Rotation.from(playerPos, targetPos);
      }
   }

   public static Rotation getRotationDown(class_2338 pos, class_2350 face) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         class_243 targetPos = class_243.method_24953(pos).method_1031(face.method_10148() * 0.501, face.method_10164() * 0.301, face.method_10165() * 0.501);
         class_243 playerPos = mc.field_1724.method_33571();
         return Rotation.from(playerPos, targetPos);
      }
   }

   public static Rotation getSmoothRotation(class_2338 pos, class_2350 face, Rotation current) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         Rotation target = getRotation(pos, face);
         return target == null ? null : smoothRotation(current, target, 0.15F);
      }
   }

   public static Rotation getSmoothRotation(class_2338 pos, class_2350 face, Rotation current, float smoothFactor) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         Rotation target = getRotation(pos, face);
         return target == null ? null : smoothRotation(current, target, smoothFactor);
      }
   }

   public static Rotation getSmoothRotationDown(class_2338 pos, class_2350 face, Rotation current, float smoothFactor) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         Rotation target = getRotationDown(pos, face);
         return target == null ? null : smoothRotation(current, target, smoothFactor);
      }
   }

   public static Rotation getSmoothRotationDown1(class_2338 pos, class_2350 face, Rotation current, float smoothFactor) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         Rotation target = getRotationDown1(pos, face);
         return target == null ? null : smoothRotation(current, target, smoothFactor);
      }
   }

   public static Rotation getRotationDown1(class_2338 pos, class_2350 face) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         class_243 targetPos = class_243.method_24953(pos).method_1031(face.method_10148() * 0.5, face.method_10164() * 0.5, face.method_10165() * 0.5);
         class_243 playerPos = mc.field_1724.method_33571();
         class_243 direction = targetPos.method_1020(playerPos);
         float currentYaw = mc.field_1724.method_36454();
         double yawRadians = Math.toRadians(currentYaw);
         double horizontalDistance = direction.field_1352 * -Math.sin(yawRadians) + direction.field_1350 * Math.cos(yawRadians);
         float pitch = (float)Math.toDegrees(-Math.atan2(direction.field_1351, Math.abs(horizontalDistance)));
         return new Rotation(currentYaw, pitch);
      }
   }

   private static Rotation smoothRotation(Rotation current, Rotation target, float factor) {
      float yawDiff = wrapDegrees(target.getYaw() - current.getYaw());
      float pitchDiff = target.getPitch() - current.getPitch();
      if (Math.abs(yawDiff) < 0.1F && Math.abs(pitchDiff) < 0.1F) {
         return target.copy();
      } else {
         float newYaw = current.getYaw() + yawDiff * factor;
         float newPitch = current.getPitch() + pitchDiff * factor;
         newPitch = class_3532.method_15363(newPitch, -90.0F, 90.0F);
         return new Rotation(newYaw, newPitch);
      }
   }

   public static void startSmoothRotation(class_2338 pos, class_2350 face) {
      if (mc.field_1724 != null) {
         targetRotation = getRotation(pos, face);
         currentRotation = new Rotation(mc.field_1724.method_36454(), mc.field_1724.method_36455());
         isRotating = true;
      }
   }

   public static Rotation updateRotation(Rotation current) {
      if (isRotating && mc.field_1724 != null && targetRotation != null) {
         currentRotation = current;
         Rotation smoothed = smoothRotation(currentRotation, targetRotation, 0.15F);
         if (smoothed.distance(targetRotation) < 0.1F) {
            isRotating = false;
            return targetRotation.copy();
         } else {
            return smoothed;
         }
      } else {
         return null;
      }
   }

   public static Rotation getConstrainedRotation(class_2338 pos, class_2350 face, float maxYawChange, float maxPitchChange) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         Rotation target = getRotation(pos, face);
         if (target == null) {
            return null;
         } else {
            Rotation current = new Rotation(mc.field_1724.method_36454(), mc.field_1724.method_36455());
            float yawDiff = wrapDegrees(target.getYaw() - current.getYaw());
            float pitchDiff = target.getPitch() - current.getPitch();
            yawDiff = class_3532.method_15363(yawDiff, -maxYawChange, maxYawChange);
            pitchDiff = class_3532.method_15363(pitchDiff, -maxPitchChange, maxPitchChange);
            float newYaw = current.getYaw() + yawDiff;
            float newPitch = class_3532.method_15363(current.getPitch() + pitchDiff, -90.0F, 90.0F);
            return new Rotation(newYaw, newPitch);
         }
      }
   }

   private static float wrapDegrees(float degrees) {
      degrees %= 360.0F;
      if (degrees >= 180.0F) {
         degrees -= 360.0F;
      }

      if (degrees < -180.0F) {
         degrees += 360.0F;
      }

      return degrees;
   }

   public static boolean isRotating() {
      return isRotating;
   }

   public static void stopRotation() {
      isRotating = false;
      targetRotation = null;
   }
}
