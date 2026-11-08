package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.SprintPacketSendEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.mixininterfaces.IVec3d;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.Set;
import lombok.Generated;
import net.minecraft.class_10182;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2189;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2399;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2708;
import net.minecraft.class_2709;
import net.minecraft.class_310;
import net.minecraft.class_3481;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_744;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class PlayerUtils implements MinecraftAccessor {
   public static int playerStuckTicks = 0;
   public static int offGroundTicks = 0;
   public static int onGroundTicks = 0;
   public static int lastOnGroundTicks = 0;
   public static int ticksSinceTeleport = 0;
   public static int ticksSinceSprint = 0;
   public static double lastPitchDiff = Double.MIN_VALUE;
   public static double lastPlacePitchDiff = Double.MIN_VALUE;
   public static boolean serverSprinting = false;
   private static class_243 lastVelocity = new class_243(0.0, 0.0, 0.0);

   public void init() {
      Southside.INSTANCE.getEventBus().register(this);
   }

   public static boolean isMoving() {
      return mc.field_1724 == null ? false : mc.field_1724.field_6212 != 0.0F || mc.field_1724.field_6250 != 0.0F;
   }

   public static void stopSprint() {
      mc.field_1724.method_5728(false);
      mc.field_1690.field_1867.method_23481(false);
   }

   public static boolean isSprintMovementKeyHold() {
      return mc.field_1690.field_1894.method_1434() && !mc.field_1690.field_1881.method_1434();
   }

   public static class_10182 resolvePos(class_10182 newPos, Set<class_2709> flags) {
      if (mc.field_1724 == null) {
         return newPos;
      } else {
         class_10182 current = class_10182.method_63638(mc.field_1724);
         return class_10182.method_63639(current, newPos, flags);
      }
   }

   public static float getSpeed(class_1657 player) {
      return player != null && mc.field_1687 != null
         ? (float)Math.sqrt(
            player.method_18798().field_1352 * player.method_18798().field_1352 + player.method_18798().field_1350 * player.method_18798().field_1350
         )
         : 0.0F;
   }

   public static int getMoveSpeedEffectAmplifier() {
      return mc.field_1724.method_6059(class_1294.field_5904) ? mc.field_1724.method_6112(class_1294.field_5904).method_5578() + 1 : 0;
   }

   public static boolean isOnLadder() {
      class_2680 state = mc.field_1687.method_8320(mc.field_1724.method_24515());
      return state.method_26164(class_3481.field_22414)
         || !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 < 0.0 && state.method_26204() instanceof class_2399;
   }

   public static boolean inFov(class_1297 entity, double fov) {
      if (fov >= 360.0) {
         return true;
      } else {
         Rotation angle = RotationUtils.calculate(mc.field_1724.method_5836(1.0F), entity.method_5829().method_1005());
         double xDist = class_3532.method_15356(angle.yaw, mc.field_1724.method_36454());
         double yDist = class_3532.method_15356(angle.pitch, mc.field_1724.method_36455());
         double angleDistance = Math.hypot(xDist, yDist);
         return angleDistance <= fov;
      }
   }

   public static boolean isAboveVoid() {
      if (mc.field_1724 == null) {
         return false;
      } else if (mc.field_1724.method_23318() < 0.0) {
         return true;
      } else {
         for (int i = (int)(mc.field_1724.method_23318() - 1.0); i >= 1; i--) {
            if (!mc.field_1687.method_8320(class_2338.method_49637(mc.field_1724.method_23317(), i, mc.field_1724.method_23321())).method_26215()) {
               return false;
            }
         }

         return !mc.field_1724.method_24828();
      }
   }

   public static double[] forward(double d) {
      float f = mc.field_1724.field_3913.field_3905;
      float f2 = mc.field_1724.field_3913.field_3907;
      float f3 = mc.field_1724.method_36454();
      if (f != 0.0F) {
         if (f2 > 0.0F) {
            f3 += f > 0.0F ? -45 : 45;
         } else if (f2 < 0.0F) {
            f3 += f > 0.0F ? 45 : -45;
         }

         f2 = 0.0F;
         if (f > 0.0F) {
            f = 1.0F;
         } else if (f < 0.0F) {
            f = -1.0F;
         }
      }

      double d2 = Math.sin(Math.toRadians(f3 + 90.0F));
      double d3 = Math.cos(Math.toRadians(f3 + 90.0F));
      double d4 = f * d * d3 + f2 * d * d2;
      double d5 = f * d * d2 - f2 * d * d3;
      return new double[]{d4, d5};
   }

   public static boolean isInLiquid() {
      if (mc.field_1724.method_5869()) {
         return true;
      } else {
         boolean inLiquid = false;
         int box = (int)mc.field_1724.method_5829().field_1322;

         for (int x = class_3532.method_15357(mc.field_1724.method_5829().field_1323);
            x < class_3532.method_15357(mc.field_1724.method_5829().field_1320) + 1;
            x++
         ) {
            for (int z = class_3532.method_15357(mc.field_1724.method_5829().field_1321);
               z < class_3532.method_15357(mc.field_1724.method_5829().field_1324) + 1;
               z++
            ) {
               class_2338 pos = new class_2338(x, box, z);
               class_2680 state = mc.field_1687.method_8320(pos);
               class_2248 b = state.method_26204();
               if (b != class_2246.field_10124 && state.method_26204() != class_2246.field_10124) {
                  if (!(b instanceof class_2404)) {
                     return false;
                  }

                  inLiquid = true;
               }
            }
         }

         return inLiquid;
      }
   }

   public static boolean canSeeEntity(class_1297 entity) {
      class_243 vec1 = new class_243(0.0, 0.0, 0.0);
      class_243 vec2 = new class_243(0.0, 0.0, 0.0);
      ((IVec3d)vec1).ss$set(mc.field_1724.method_23317(), mc.field_1724.method_23318() + mc.field_1724.method_5751(), mc.field_1724.method_23321());
      ((IVec3d)vec2).ss$set(entity.method_23317(), entity.method_23318(), entity.method_23321());
      boolean canSeeFeet = mc.field_1687.method_17742(new class_3959(vec1, vec2, class_3960.field_17558, class_242.field_1348, mc.field_1724)).method_17783()
         == class_240.field_1333;
      ((IVec3d)vec2).ss$set(entity.method_23317(), entity.method_23318() + entity.method_5751(), entity.method_23321());
      boolean canSeeEyes = mc.field_1687.method_17742(new class_3959(vec1, vec2, class_3960.field_17558, class_242.field_1348, mc.field_1724)).method_17783()
         == class_240.field_1333;
      return canSeeFeet || canSeeEyes;
   }

   public static class_2248 blockRelativeToPlayer(double offsetX, double offsetY, double offsetZ) {
      return mc.field_1687.method_8320(mc.field_1724.method_24515().method_10069((int)offsetX, (int)offsetY, (int)offsetZ)).method_26204();
   }

   public static void strafe(Float speed) {
      if (mc.field_1724 != null) {
         if (isMoving()) {
            double yaw = Math.toRadians(getPlayerDirection());
            double xSpeed = -Math.sin(yaw);
            double zSpeed = Math.cos(yaw);
            double magnitude = Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed);
            if (magnitude > 0.0) {
               xSpeed /= magnitude;
               zSpeed /= magnitude;
            }

            mc.field_1724.method_18800(xSpeed * speed.floatValue(), mc.field_1724.method_18798().field_1351, zSpeed * speed.floatValue());
         } else {
            stopMoving();
         }
      }
   }

   public static float getPlayerDirection() {
      if (mc.field_1724 == null) {
         return 0.0F;
      } else {
         float yaw = mc.field_1724.method_36454();
         float strafe = 45.0F;
         class_744 input = mc.field_1724.field_3913;
         if (input.field_3905 < 0.0F) {
            strafe = -45.0F;
            yaw += 180.0F;
         }

         if (input.field_54155.comp_3161()) {
            yaw -= strafe;
            if (input.field_3905 == 0.0F) {
               yaw -= 45.0F;
            }
         } else if (input.field_54155.comp_3162()) {
            yaw += strafe;
            if (input.field_3905 == 0.0F) {
               yaw += 45.0F;
            }
         }

         return (yaw % 360.0F + 360.0F) % 360.0F;
      }
   }

   public static void stopMoving() {
      if (mc.field_1724 != null) {
         mc.field_1724.method_18800(0.0, mc.field_1724.method_18798().field_1351, 0.0);
         mc.field_1724.field_3913.field_3905 = 0.0F;
         mc.field_1724.field_3913.field_3907 = 0.0F;
      }
   }

   public static boolean isBlockUnder(double height) {
      for (int offset = 0; offset < height; offset += 2) {
         class_238 bb = mc.field_1724.method_5829().method_989(0.0, -offset, 0.0);
         if (!mc.field_1687.method_39454(mc.field_1724, bb)) {
            return true;
         }
      }

      return false;
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getOrigin().equals(TransferOrigin.RECEIVE) && event.getPacket() instanceof class_2708) {
         ticksSinceTeleport = 0;
      }
   }

   @EventHandler
   public void onPostTick(TickEvent e) {
      if (e.getState().equals(Stage.POST) && mc.field_1724 != null) {
         lastVelocity = mc.field_1724.method_18798();
      }
   }

   public static float wrapDegrees(float degrees) {
      float f = degrees % 360.0F;
      if (f >= 180.0F) {
         f -= 360.0F;
      }

      if (f < -180.0F) {
         f += 360.0F;
      }

      return f;
   }

   public static float direction(float forward, float strafe) {
      float direction = class_310.method_1551().field_1724.method_36454();
      boolean isMovingForward = forward > 0.0F;
      boolean isMovingBack = forward < 0.0F;
      boolean isMovingRight = strafe > 0.0F;
      boolean isMovingLeft = strafe < 0.0F;
      boolean isMovingSideways = isMovingRight || isMovingLeft;
      boolean isMovingStraight = isMovingForward || isMovingBack;
      if (forward != 0.0F || strafe != 0.0F) {
         if (isMovingBack && !isMovingSideways) {
            return direction + 180.0F;
         }

         if (isMovingForward && isMovingLeft) {
            return direction + 45.0F;
         }

         if (isMovingForward && isMovingRight) {
            return direction - 45.0F;
         }

         if (!isMovingStraight && isMovingLeft) {
            return direction + 90.0F;
         }

         if (!isMovingStraight && isMovingRight) {
            return direction - 90.0F;
         }

         if (isMovingBack && isMovingLeft) {
            return direction + 135.0F;
         }

         if (isMovingBack) {
            return direction - 135.0F;
         }
      }

      return direction;
   }

   public static void flushBoundingBoxes() {
      if (mc.field_1687 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            entity.method_33574(entity.method_19538());
         }
      }
   }

   public static void moveFlying(double speed) {
      class_1657 player = mc.field_1724;
      if (player != null && isMoving()) {
         double yawRad = Math.toRadians(direction(mc.field_1724.field_3913.field_3905, mc.field_1724.field_3913.field_3907));
         double x = -class_3532.method_15374((float)yawRad) * speed;
         double z = class_3532.method_15362((float)yawRad) * speed;
         player.method_5762(x, 0.0, z);
      }
   }

   public static boolean willFallNextTick() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         FallingPlayer pred = new FallingPlayer(mc.field_1724);
         pred.calculate(1);
         class_2338 frontPos1 = new class_2338((int)pred.getX(), (int)(mc.field_1724.method_23318() - 0.1), (int)pred.getZ());
         return mc.field_1687.method_8320(frontPos1).method_26204() instanceof class_2189 && pred.getMotion().field_1351 < 0.0;
      }
   }

   public static void setSkipTicks(int i) {
      playerStuckTicks = i;
   }

   @EventHandler(
      priority = 200
   )
   public void onSprint(SprintPacketSendEvent event) {
      if (!event.isSprinting()) {
         ticksSinceSprint = 0;
      } else {
         ticksSinceSprint++;
      }
   }

   @Generated
   public static class_243 getLastVelocity() {
      return lastVelocity;
   }
}
