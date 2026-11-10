package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.player.DirectionalInput;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;

public class EntityUtils {
   public static Vec3d withStrafe(Vec3d vec, double speed, double strength) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.player != null) {
         boolean forward = mc.options.forwardKey.isPressed();
         boolean back = mc.options.backKey.isPressed();
         boolean left = mc.options.leftKey.isPressed();
         boolean right = mc.options.rightKey.isPressed();
         boolean isMoving = forward || back || left || right;
         float movementYaw = computeMovementYaw(mc.player.getYaw(), forward, back, left, right);
         return withStrafe(vec, speed, strength, isMoving, movementYaw);
      } else {
         return vec;
      }
   }

   public static Vec3d withStrafe(Vec3d vec, double speed, double strength, boolean isMoving, float yaw) {
      if (!isMoving) {
         return new Vec3d(0.0, vec.y, 0.0);
      } else {
         double prevX = vec.x * (1.0 - strength);
         double prevZ = vec.z * (1.0 - strength);
         double useSpeed = speed * strength;
         double angle = Math.toRadians(yaw);
         double newX = -Math.sin(angle) * useSpeed + prevX;
         double newZ = Math.cos(angle) * useSpeed + prevZ;
         return new Vec3d(newX, vec.y, newZ);
      }
   }

   public static Vec3d withStrafe(Vec3d vec, double speed) {
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
      if (MinecraftAccessor.mc.player == null) {
         return false;
      } else {
         ItemStack main = MinecraftAccessor.mc.player.getMainHandStack();
         ItemStack off = MinecraftAccessor.mc.player.getOffHandStack();
         return !main.isEmpty() && !off.isEmpty()
            ? main.getItem() == off.getItem() || isInteractive(main.getUseAction()) && isInteractive(off.getUseAction())
            : false;
      }
   }

   public static boolean isInteractive(UseAction anim) {
      return anim == UseAction.EAT || anim == UseAction.DRINK || anim == UseAction.BOW || anim == UseAction.CROSSBOW;
   }

   public static boolean isRemote(UseAction anim) {
      return anim == UseAction.BOW || anim == UseAction.CROSSBOW || anim == UseAction.SPEAR;
   }

   public static boolean isCharged(ItemStack stack) {
      return stack != null && stack.getItem() instanceof CrossbowItem ? CrossbowItem.isCharged(stack) : false;
   }
}
