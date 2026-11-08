package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.render.Render3D;
import dev.southside.utils.item.ItemUtils;
import dev.southside.utils.network.PacketUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1667;
import net.minecraft.class_1676;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1812;
import net.minecraft.class_1934;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2824;
import net.minecraft.class_2827;

public class FakeLagTest extends Module {
   private final Slider maxPacketsValue = new Slider("Max Packets", 50, 1, 200, 1);
   private final Switch slowReleaseValue = new Switch("Slow Release", false);
   private final Switch ignoreKeepAlive = new Switch("Ignore KeepAlive", false);
   private final Switch antiAimValue = new Switch("Anti Aim", true);
   private final Switch skipConsumablesValue = new Switch("Skip Consumables", true);
   private final Switch includeTeamsValue = new Switch("Teams", false);
   private final Slider dangerRangeValue = new Slider("Danger Range", 4.0, 1.0, 8.0, 0.1);
   private final Slider attackSyncDistanceValue = new Slider("Attack Sync Distance", 3.0, 1.0, 6.0, 0.1);
   private final Color boxColor = new Color("Box Color", new OneColor(64, 176, 255, 80));
   private final LinkedList<List<class_2596<?>>> packets = new LinkedList<>();
   private int ticks;
   private final List<class_243> realPos = new ArrayList<>();
   private class_243 renderPos;

   public FakeLagTest() {
      super(Category.Misc, "FakeLagTest", "Blink-style fake lag");
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.packets.clear();
         this.packets.add(new ArrayList<>());
         this.ticks = 0;
         this.realPos.clear();
         this.renderPos = null;
         class_243 startPos = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
         this.realPos.add(startPos);
         this.renderPos = startPos;
         mc.field_1724.method_31549().field_7476 = true;
         mc.field_1724.method_51889(class_1934.field_9215);
         super.onEnable();
      } else {
         this.setEnable(false);
      }
   }

   @Override
   public void onDisable() {
      this.packets.forEach(this::sendTick);
      this.packets.clear();
      this.realPos.clear();
      this.ticks = 0;
      super.onDisable();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.setEnable(false);
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if (this.isEnabled() && mc.field_1724 != null) {
         class_243 serverPos = this.getServerPosition();
         if (serverPos != null) {
            if (this.renderPos == null) {
               this.renderPos = serverPos;
            } else {
               class_243 diff = serverPos.method_1020(this.renderPos);
               double maxStep = 0.35;
               double distance = diff.method_1033();
               if (distance > maxStep) {
                  class_243 step = diff.method_1029().method_1021(maxStep);
                  this.renderPos = this.renderPos.method_1019(step);
               } else {
                  this.renderPos = serverPos;
               }
            }

            double halfWidth = mc.field_1724.method_17681() / 2.0;
            class_238 box = new class_238(
               this.renderPos.field_1352 - halfWidth,
               this.renderPos.field_1351,
               this.renderPos.field_1350 - halfWidth,
               this.renderPos.field_1352 + halfWidth,
               this.renderPos.field_1351 + mc.field_1724.method_17682(),
               this.renderPos.field_1350 + halfWidth
            );
            Render3D.boxSides(box, this.boxColor.getValue().toJavaColor());
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      class_2596<?> packet = event.getPacket();
      if (!this.ignoreKeepAlive.getValue() || !(packet instanceof class_2827)) {
         if (event.getOrigin() == TransferOrigin.SEND) {
            if (this.shouldSkipForConsumable()) {
               return;
            }

            if (this.packets.isEmpty()) {
               this.packets.add(new LinkedList<>());
            }

            this.packets.getLast().add(packet);
            event.setCancelled(true);
            if (packet instanceof class_2824 interact && interact.field_12871 == class_2824.field_29170) {
               this.handleAttackRelease(interact);
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      this.ticks++;
      this.packets.add(new ArrayList<>());
      if (mc.field_1724 != null) {
         class_243 pos = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
         this.realPos.add(pos);
         if (this.renderPos == null) {
            this.renderPos = pos;
         }
      }

      if (this.packets.size() > this.maxPacketsValue.getValue().intValue()) {
         this.poll();
      } else if (this.slowReleaseValue.getValue() && this.ticks % 10 == 0) {
         this.flushUntilSafe();
      }

      if (this.antiAimValue.getValue()) {
         while (true) {
            boolean dangerous = false;
            if (mc.field_1687 != null) {
               for (class_1297 entity : mc.field_1687.method_18112()) {
                  if (entity instanceof class_1667 arrow && !arrow.method_65059() && this.isDangerous(arrow)) {
                     dangerous = true;
                     break;
                  }
               }
            }

            if (!dangerous || this.packets.size() < 3) {
               break;
            }

            this.poll();
         }
      }

      this.flushUntilSafe();
   }

   private boolean isDangerous(class_1297 entity) {
      float width = 1.2F;
      float height = 2.2F;
      if (entity instanceof class_1676 projectile) {
         float motionSlowdown = 0.99F;
         float size = 1.2F;
         float gravity = 0.05F;
         return ProjectionUtil.predictBox(
            entity.method_23317(),
            entity.method_23318(),
            entity.method_23321(),
            entity.method_18798().field_1352,
            entity.method_18798().field_1351,
            entity.method_18798().field_1350,
            motionSlowdown,
            size,
            gravity,
            this.realPos,
            0.6F,
            2.2F
         );
      } else {
         return false;
      }
   }

   private void poll() {
      if (!this.packets.isEmpty()) {
         this.sendTick(this.packets.getFirst());
         this.packets.removeFirst();
      }
   }

   private void sendTick(List<class_2596<?>> tick) {
      if (Objects.requireNonNull(mc.method_1562()).method_48296() != null) {
         tick.forEach(PacketUtils::sendPacketNoEvent);
      }

      if (!this.realPos.isEmpty()) {
         this.realPos.remove(0);
      }
   }

   private void handleAttackRelease(class_2824 interactPacket) {
      if (mc.field_1687 != null && !this.realPos.isEmpty()) {
         if (mc.field_1687.method_8469(interactPacket.field_12870) instanceof class_1657 target) {
            class_243 targetPos = target.method_19538();
            class_243 serverPos = this.getServerPosition();
            if (serverPos != null) {
               double desiredDistance = this.attackSyncDistanceValue.getValue().doubleValue();
               double currentDistance = serverPos.method_1022(targetPos);

               while (currentDistance > desiredDistance && !this.packets.isEmpty()) {
                  this.poll();
                  serverPos = this.getServerPosition();
                  if (serverPos == null) {
                     break;
                  }

                  currentDistance = serverPos.method_1022(targetPos);
               }
            }
         }
      }
   }

   private class_243 getServerPosition() {
      return this.realPos.isEmpty() ? null : this.realPos.get(0);
   }

   private boolean shouldReleaseForSafety() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 serverPos = this.getServerPosition();
         if (serverPos == null) {
            return false;
         } else {
            double range = this.dangerRangeValue.getValue().doubleValue();
            class_243 delta = serverPos.method_1020(mc.field_1724.method_19538());
            class_238 serverBox = mc.field_1724.method_5829().method_997(delta);

            for (class_1657 entity : mc.field_1687.method_18456()) {
               if (entity != mc.field_1724
                  && entity.method_5805()
                  && !entity.method_7325()
                  && (this.includeTeamsValue.getValue() || !Teams.isTeam(entity))
                  && !(entity.method_5858(mc.field_1724) > range * range)) {
                  boolean intersects = serverBox.method_994(entity.method_5829());
                  double serverDistance = entity.method_5649(serverPos.field_1352, serverPos.field_1351, serverPos.field_1350);
                  double clientDistance = entity.method_5858(mc.field_1724);
                  if (intersects || serverDistance < clientDistance) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private void flushUntilSafe() {
      if (this.shouldReleaseForSafety()) {
         while (!this.packets.isEmpty() && this.shouldReleaseForSafety()) {
            this.poll();
         }
      }
   }

   private boolean shouldSkipForConsumable() {
      if (!this.skipConsumablesValue.getValue() || mc.field_1724 == null) {
         return false;
      } else if (!mc.field_1724.method_6115()) {
         return false;
      } else {
         class_1799 stack = mc.field_1724.method_6030();
         return stack.method_7960()
            ? false
            : ItemUtils.isConsumable(stack) || stack.method_7909() == class_1802.field_8103 || stack.method_7909() instanceof class_1812;
      }
   }
}
