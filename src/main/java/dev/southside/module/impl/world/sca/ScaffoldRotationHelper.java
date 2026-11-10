package dev.southside.module.impl.world.sca;

import dev.southside.utils.types.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class ScaffoldRotationHelper {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final float SMOOTH_FACTOR = 0.15F;
   private static final float MIN_ANGLE_DIFF = 0.1F;
   private static Rotation targetRotation = null;
   private static Rotation currentRotation = null;
   private static boolean isRotating = false;

   public static Rotation getRotation(BlockPos pos, Direction face) {
      if (mc.player == null) {
         return null;
      } else {
         Vec3d targetPos = Vec3d.ofCenter(pos).add(face.getOffsetX() * 0.501, face.getOffsetY() * 0.501, face.getOffsetZ() * 0.501);
         Vec3d playerPos = mc.player.getEyePos();
         return Rotation.from(playerPos, targetPos);
      }
   }

   public static Rotation getRotationDown(BlockPos pos, Direction face) {
      if (mc.player == null) {
         return null;
      } else {
         Vec3d targetPos = Vec3d.ofCenter(pos).add(face.getOffsetX() * 0.501, face.getOffsetY() * 0.301, face.getOffsetZ() * 0.501);
         Vec3d playerPos = mc.player.getEyePos();
         return Rotation.from(playerPos, targetPos);
      }
   }

   public static Rotation getSmoothRotation(BlockPos pos, Direction face, Rotation current) {
      if (mc.player == null) {
         return null;
      } else {
         Rotation target = getRotation(pos, face);
         return target == null ? null : smoothRotation(current, target, 0.15F);
      }
   }

   public static Rotation getSmoothRotation(BlockPos pos, Direction face, Rotation current, float smoothFactor) {
      if (mc.player == null) {
         return null;
      } else {
         Rotation target = getRotation(pos, face);
         return target == null ? null : smoothRotation(current, target, smoothFactor);
      }
   }

   public static Rotation getSmoothRotationDown(BlockPos pos, Direction face, Rotation current, float smoothFactor) {
      if (mc.player == null) {
         return null;
      } else {
         Rotation target = getRotationDown(pos, face);
         return target == null ? null : smoothRotation(current, target, smoothFactor);
      }
   }

   public static Rotation getSmoothRotationDown1(BlockPos pos, Direction face, Rotation current, float smoothFactor) {
      if (mc.player == null) {
         return null;
      } else {
         Rotation target = getRotationDown1(pos, face);
         return target == null ? null : smoothRotation(current, target, smoothFactor);
      }
   }

   public static Rotation getRotationDown1(BlockPos pos, Direction face) {
      if (mc.player == null) {
         return null;
      } else {
         Vec3d targetPos = Vec3d.ofCenter(pos).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
         Vec3d playerPos = mc.player.getEyePos();
         Vec3d direction = targetPos.subtract(playerPos);
         float currentYaw = mc.player.getYaw();
         double yawRadians = Math.toRadians(currentYaw);
         double horizontalDistance = direction.x * -Math.sin(yawRadians) + direction.z * Math.cos(yawRadians);
         float pitch = (float)Math.toDegrees(-Math.atan2(direction.y, Math.abs(horizontalDistance)));
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
         newPitch = MathHelper.clamp(newPitch, -90.0F, 90.0F);
         return new Rotation(newYaw, newPitch);
      }
   }

   public static void startSmoothRotation(BlockPos pos, Direction face) {
      if (mc.player != null) {
         targetRotation = getRotation(pos, face);
         currentRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
         isRotating = true;
      }
   }

   public static Rotation updateRotation(Rotation current) {
      if (isRotating && mc.player != null && targetRotation != null) {
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

   public static Rotation getConstrainedRotation(BlockPos pos, Direction face, float maxYawChange, float maxPitchChange) {
      if (mc.player == null) {
         return null;
      } else {
         Rotation target = getRotation(pos, face);
         if (target == null) {
            return null;
         } else {
            Rotation current = new Rotation(mc.player.getYaw(), mc.player.getPitch());
            float yawDiff = wrapDegrees(target.getYaw() - current.getYaw());
            float pitchDiff = target.getPitch() - current.getPitch();
            yawDiff = MathHelper.clamp(yawDiff, -maxYawChange, maxYawChange);
            pitchDiff = MathHelper.clamp(pitchDiff, -maxPitchChange, maxPitchChange);
            float newYaw = current.getYaw() + yawDiff;
            float newPitch = MathHelper.clamp(current.getPitch() + pitchDiff, -90.0F, 90.0F);
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
