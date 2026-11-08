package dev.southside.utils.player;

import dev.southside.event.Stage;
import dev.southside.event.events.EntityMoveEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import java.util.Optional;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2664;
import net.minecraft.class_2743;
import net.minecraft.class_310;

public final class MovementUtils {
   public static final MovementUtils INSTANCE = new MovementUtils();
   private static int noMoveTicks = 0;
   public static boolean cancelMove = false;
   public static class_243 motion;
   private static float fallDistance = 0.0F;
   private static int cancelTicks = 0;
   private static class_310 mc = class_310.method_1551();
   static boolean shouldRelease;
   private static boolean holdInputRequested = false;

   public static void cancelMove() {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1724.method_18798() != null) {
            if (!shouldRelease) {
               if (mc.field_1724 != null) {
                  if (!cancelMove) {
                     cancelMove = true;
                     motion = mc.field_1724.method_18798();
                     fallDistance = mc.field_1724.field_6017;
                  }
               }
            }
         }
      }
   }

   public static void cancelByTicks(int ticks) {
      if (!ClientUtils.nullCheck()) {
         cancelTicks = ticks;
      }
   }

   public static double direction() {
      float rotationYaw = mc.field_1724.method_36454();
      if (mc.field_1724.field_6250 < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (mc.field_1724.field_6250 < 0.0F) {
         forward = -0.5F;
      } else if (mc.field_1724.field_6250 > 0.0F) {
         forward = 0.5F;
      }

      if (mc.field_1724.field_6212 > 0.0F) {
         rotationYaw -= 70.0F * forward;
      }

      if (mc.field_1724.field_6212 < 0.0F) {
         rotationYaw += 70.0F * forward;
      }

      return Math.toRadians(rotationYaw);
   }

   public static void resetMove() {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1724.method_18798() != null && motion != null) {
            cancelMove = false;
            noMoveTicks = 0;
            mc.field_1724.method_18799(motion);
            motion = null;
            mc.field_1724.field_6017 = fallDistance;
         }
      }
   }

      public static void requestInputHold() {
      holdInputRequested = true;
   }

   public static boolean consumeInputHold() {
      boolean requested = holdInputRequested;
      holdInputRequested = false;
      return requested;
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1724.method_18798() != null) {
            if (cancelTicks > 0) {
               cancelTicks--;
               cancelMove();
            }

            if (cancelMove) {
               mc.field_1724.method_18799(motion);
               mc.field_1724.field_6017 = fallDistance;
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1724.method_18798() != null) {
            if (event.getState() == Stage.PRE) {
               shouldRelease = false;
               resetMove();
            } else {
               if (cancelMove) {
                  mc.field_1724.method_18799(motion);
                  mc.field_1724.field_6017 = fallDistance;
               }
            }
         }
      }
   }

   @EventHandler
   public void onMove(EntityMoveEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1724.method_18798() != null) {
            if (cancelMove) {
               event.setCancelled(true);
            }
         }
      }
   }

   public static void onPacketRec(class_2596 rec) {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1724.method_18798() != null) {
            if (rec instanceof class_2743 packet && cancelMove && mc.field_1724.method_5628() == packet.method_11818()) {
               motion = new class_243(packet.method_11815() / 8000.0, packet.method_11816() / 8000.0, packet.method_11819() / 8000.0);
            }

            if (rec instanceof class_2664 packet && cancelMove) {
               Optional<class_243> velo = packet.comp_2884();
               velo.ifPresent(vec3d -> motion.method_1031(vec3d.field_1352, vec3d.field_1351, vec3d.field_1350));
            }
         }
      }
   }
}
