package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.player.DirectionalInput;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1839;
import net.minecraft.class_243;
import net.minecraft.class_310;

public class EntityUtils {
   public static class_243 withStrafe(class_243 vec, double speed, double strength) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1724 != null) {
         boolean forward = mc.field_1690.field_1894.method_1434();
         boolean back = mc.field_1690.field_1881.method_1434();
         boolean left = mc.field_1690.field_1913.method_1434();
         boolean right = mc.field_1690.field_1849.method_1434();
         boolean isMoving = forward || back || left || right;
         float movementYaw = computeMovementYaw(mc.field_1724.method_36454(), forward, back, left, right);
         return withStrafe(vec, speed, strength, isMoving, movementYaw);
      } else {
         return vec;
      }
   }

   public static class_243 withStrafe(class_243 vec, double speed, double strength, boolean isMoving, float yaw) {
      if (!isMoving) {
         return new class_243(0.0, vec.field_1351, 0.0);
      } else {
         double prevX = vec.field_1352 * (1.0 - strength);
         double prevZ = vec.field_1350 * (1.0 - strength);
         double useSpeed = speed * strength;
         double angle = Math.toRadians(yaw);
         double newX = -Math.sin(angle) * useSpeed + prevX;
         double newZ = Math.cos(angle) * useSpeed + prevZ;
         return new class_243(newX, vec.field_1351, newZ);
      }
   }

   public static class_243 withStrafe(class_243 vec, double speed) {
      return withStrafe(vec, speed, 1.0);
   }

   private static float computeMovementYaw(float playerYaw, boolean forward, boolean back, boolean left, boolean right) {
      float yaw = playerYaw;
      if (forward) {
         if (left) {
            yaw = playerYaw + 45.0F;
         } else if (right) {
            yaw = playerYaw - 45.0F;
         }
      } else if (back) {
         yaw = playerYaw + 180.0F;
         if (left) {
            yaw -= 45.0F;
         } else if (right) {
            yaw += 45.0F;
         }
      } else if (left) {
         yaw = playerYaw + 90.0F;
      } else if (right) {
         yaw = playerYaw - 90.0F;
      }

      return yaw;
   }

   public static float getMovementDirectionOfInput(float facingYaw, DirectionalInput input) {
      boolean forwards = input.isForward() && !input.isBack();
      boolean backwards = input.isBack() && !input.isForward();
      boolean left = input.isLeft() && !input.isRight();
      boolean right = input.isRight() && !input.isLeft();
      float actualYaw = facingYaw;
      float forward = 1.0F;
      if (backwards) {
         actualYaw = facingYaw + 180.0F;
         forward = -0.5F;
      } else if (forwards) {
         forward = 0.5F;
      }

      if (left) {
         actualYaw -= 90.0F * forward;
      }

      if (right) {
         actualYaw += 90.0F * forward;
      }

      return actualYaw;
   }

   public static boolean isConflict() {
      if (MinecraftAccessor.mc.field_1724 == null) {
         return false;
      } else {
         class_1799 main = MinecraftAccessor.mc.field_1724.method_6047();
         class_1799 off = MinecraftAccessor.mc.field_1724.method_6079();
         return !main.method_7960() && !off.method_7960()
            ? main.method_7909() == off.method_7909() || isInteractive(main.method_7976()) && isInteractive(off.method_7976())
            : false;
      }
   }

   public static boolean isInteractive(class_1839 anim) {
      return anim == class_1839.field_8950 || anim == class_1839.field_8946 || anim == class_1839.field_8953 || anim == class_1839.field_8947;
   }

   public static boolean isRemote(class_1839 anim) {
      return anim == class_1839.field_8953 || anim == class_1839.field_8947 || anim == class_1839.field_8951;
   }

   public static boolean isCharged(class_1799 stack) {
      return stack != null && stack.method_7909() instanceof class_1764 ? class_1764.method_7781(stack) : false;
   }
}
