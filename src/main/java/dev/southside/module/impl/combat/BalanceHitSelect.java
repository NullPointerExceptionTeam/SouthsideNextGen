package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.Teams;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class BalanceHitSelect extends Module {
   private long lastEnterTime = 0L;
   private boolean isInRange = false;
   public Slider startReach = new Slider("Start Reach", 4.0, 0.5, 10.0, 0.01);
   public Slider attackReach = new Slider("Attack Reach", 3.0, 1.0, 6.0, 0.01);
   public Switch fovCheck = new Switch("FOV Check", true);
   public Switch distanceCheck = new Switch("Distance Check", true);
   public Switch debug = new Switch("Debug", true);
   private final BalanceHitSelect.PacketManager packetManager = new BalanceHitSelect.PacketManager(PacketUtils::sendPacketNoEvent);

   public BalanceHitSelect() {
      super(Category.Combat, "Balance Hitselect", "Balance hit select with blink");
   }

   public static PlayerEntity getClosestPlayer() {
      PlayerEntity closestPlayer = null;
      double closestDistance = Double.MAX_VALUE;
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world != null) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player) {
               double distance = 0.0;
               if (mc.player != null) {
                  distance = mc.player.distanceTo(player);
               }

               if (distance < closestDistance) {
                  closestDistance = distance;
                  closestPlayer = player;
               }
            }
         }
      }

      return closestPlayer;
   }

   private boolean isInFOV(PlayerEntity target, double fov) {
      if (mc.player != null && target != null) {
         double diffX = target.getX() - mc.player.getX();
         double diffZ = target.getZ() - mc.player.getZ();
         double angle = Math.toDegrees(Math.atan2(diffZ, diffX)) - mc.player.getYaw() - 90.0;
         angle = MathHelper.wrapDegrees(angle);
         return Math.abs(angle) <= fov / 2.0;
      } else {
         return false;
      }
   }

   @EventHandler
   public void onUpdate(TickEvent event) {
      if (event.state == Stage.PRE) {
         if (mc.player != null && mc.player.isAlive() && !mc.player.isDead()) {
            PlayerEntity closestPlayer = getClosestPlayer();
            if (closestPlayer != null && !Teams.isTeam(closestPlayer)) {
               double startReachValue = this.startReach.getValue().doubleValue();
               double attackReachValue = this.attackReach.getValue().doubleValue();
               double distance = mc.player.distanceTo(closestPlayer);
               boolean inFOV = !this.fovCheck.getValue() || this.isInFOV(closestPlayer, 180.0);
               boolean shouldCache = distance < startReachValue && inFOV;
               if (this.distanceCheck.getValue()) {
                  shouldCache = shouldCache && distance >= attackReachValue;
               }

               if (shouldCache) {
                  if (!this.isInRange) {
                     if (mc.world != null && mc.player != null && this.debug.getValue()) {
                        this.info(String.format("§2[BHS] Cache | Distance: %s", distance), new Object[0]);
                     }

                     this.isInRange = true;
                     this.lastEnterTime = System.currentTimeMillis();
                  }
               } else if (this.isInRange) {
                  String reason = "Condition not met";
                  if (this.distanceCheck.getValue() && distance < attackReachValue) {
                     reason = "Too Close";
                  } else if (distance >= startReachValue) {
                     reason = "Too Far";
                  } else if (this.fovCheck.getValue() && !this.isInFOV(closestPlayer, 180.0)) {
                     reason = "Out of FOV";
                  }

                  if (mc.world != null && mc.player != null && this.debug.getValue()) {
                     this.info(String.format("§4[BHS] Release | %s | Distance: %s", reason, distance), new Object[0]);
                  }

                  this.packetManager.release();
                  this.isInRange = false;
               }

               if (this.isInRange && System.currentTimeMillis() - this.lastEnterTime >= 750L) {
                  if (mc.world != null && mc.player != null && this.debug.getValue()) {
                     this.info("§4[BHS] Release | Timeout", new Object[0]);
                  }

                  this.packetManager.release();
                  this.isInRange = false;
               }
            } else if (this.isInRange) {
               if (mc.world != null && mc.player != null && this.debug.getValue()) {
                  this.info("§4[BHS] Release | No Player", new Object[0]);
               }

               this.packetManager.release();
               this.isInRange = false;
            }
         } else {
            this.packetManager.release();
         }
      }
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (event.stage == Stage.POST && this.isInRange) {
         if (mc.world != null && mc.player != null && this.debug.getValue()) {
            this.info("§4[BHS] Release | Attack", new Object[0]);
         }

         this.packetManager.release();
         this.isInRange = false;
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.isSend() && this.isInRange) {
         this.packetManager.cache(event.packet);
         event.setCancelled(true);
      }
   }

   public static class PacketManager {
      private final List<Packet<?>> cachedPackets = new CopyOnWriteArrayList<>();
      private final BalanceHitSelect.PacketSender packetSender;

      public PacketManager(BalanceHitSelect.PacketSender packetSender) {
         this.packetSender = packetSender;
      }

      public void cache(Packet<?> packet) {
         this.cachedPackets.add(packet);
      }

      public void release() {
         for (Packet<?> packet : this.cachedPackets) {
            this.packetSender.send(packet);
         }

         this.cachedPackets.clear();
      }

      public void clear() {
         this.cachedPackets.clear();
      }
   }

   public interface PacketSender {
      void send(Packet<?> var1);
   }
}
