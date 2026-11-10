package dev.southside.utils.player;

import dev.southside.event.Stage;
import dev.southside.event.events.EntityMoveEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import java.util.Optional;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.client.MinecraftClient;

public final class MovementUtils {
   public static final MovementUtils INSTANCE = new MovementUtils();
   private static int noMoveTicks = 0;
   public static boolean cancelMove = false;
   public static Vec3d motion;
   private static float fallDistance = 0.0F;
   private static int cancelTicks = 0;
   private static MinecraftClient mc = MinecraftClient.getInstance();
   static boolean shouldRelease;
   private static boolean holdInputRequested = false;

   public static void cancelMove() {
      if (!ClientUtils.nullCheck()) {
         if (mc.player.getVelocity() != null) {
            if (!shouldRelease) {
               if (mc.player != null) {
                  if (!cancelMove) {
                     cancelMove = true;
                     motion = mc.player.getVelocity();
                     fallDistance = mc.player.fallDistance;
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
      float rotationYaw = mc.player.getYaw();
      if (mc.player.forwardSpeed < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (mc.player.forwardSpeed < 0.0F) {
         forward = -0.5F;
      } else if (mc.player.forwardSpeed > 0.0F) {
         forward = 0.5F;
      }

      if (mc.player.sidewaysSpeed > 0.0F) {
         rotationYaw -= 70.0F * forward;
      }

      if (mc.player.sidewaysSpeed < 0.0F) {
         rotationYaw += 70.0F * forward;
      }

      return Math.toRadians(rotationYaw);
   }

   public static void resetMove() {
      if (!ClientUtils.nullCheck()) {
         if (mc.player.getVelocity() != null && motion != null) {
            cancelMove = false;
            noMoveTicks = 0;
            mc.player.setVelocity(motion);
            motion = null;
            mc.player.fallDistance = fallDistance;
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
         if (mc.player.getVelocity() != null) {
            if (cancelTicks > 0) {
               cancelTicks--;
               cancelMove();
            }

            if (cancelMove) {
               mc.player.setVelocity(motion);
               mc.player.fallDistance = fallDistance;
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.player.getVelocity() != null) {
            if (event.getState() == Stage.PRE) {
               shouldRelease = false;
               resetMove();
            } else {
               if (cancelMove) {
                  mc.player.setVelocity(motion);
                  mc.player.fallDistance = fallDistance;
               }
            }
         }
      }
   }

   @EventHandler
   public void onMove(EntityMoveEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.player.getVelocity() != null) {
            if (cancelMove) {
               event.setCancelled(true);
            }
         }
      }
   }

   public static void onPacketRec(Packet rec) {
      if (!ClientUtils.nullCheck()) {
         if (mc.player.getVelocity() != null) {
            if (rec instanceof EntityVelocityUpdateS2CPacket packet && cancelMove && mc.player.getId() == packet.getEntityId()) {
               motion = new Vec3d(packet.getVelocityX() / 8000.0, packet.getVelocityY() / 8000.0, packet.getVelocityZ() / 8000.0);
            }

            if (rec instanceof ExplosionS2CPacket packet && cancelMove) {
               Optional<Vec3d> velo = packet.playerKnockback();
               velo.ifPresent(vec3d -> motion.add(vec3d.x, vec3d.y, vec3d.z));
            }
         }
      }
   }
}
