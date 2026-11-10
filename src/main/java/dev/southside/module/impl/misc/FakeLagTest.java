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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.world.GameMode;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;

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
   private final LinkedList<List<Packet<?>>> packets = new LinkedList<>();
   private int ticks;
   private final List<Vec3d> realPos = new ArrayList<>();
   private Vec3d renderPos;

   public FakeLagTest() {
      super(Category.Misc, "FakeLagTest", "Blink-style fake lag");
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.world != null) {
         this.packets.clear();
         this.packets.add(new ArrayList<>());
         this.ticks = 0;
         this.realPos.clear();
         this.renderPos = null;
         Vec3d startPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
         this.realPos.add(startPos);
         this.renderPos = startPos;
         mc.player.getAbilities().allowModifyWorld = true;
         mc.player.onGameModeChanged(GameMode.SURVIVAL);
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
      if (this.isEnabled() && mc.player != null) {
         Vec3d serverPos = this.getServerPosition();
         if (serverPos != null) {
            if (this.renderPos == null) {
               this.renderPos = serverPos;
            } else {
               Vec3d diff = serverPos.subtract(this.renderPos);
               double maxStep = 0.35;
               double distance = diff.length();
               if (distance > maxStep) {
                  Vec3d step = diff.normalize().multiply(maxStep);
                  this.renderPos = this.renderPos.add(step);
               } else {
                  this.renderPos = serverPos;
               }
            }

            double halfWidth = mc.player.getWidth() / 2.0;
            Box box = new Box(
               this.renderPos.x - halfWidth,
               this.renderPos.y,
               this.renderPos.z - halfWidth,
               this.renderPos.x + halfWidth,
               this.renderPos.y + mc.player.getHeight(),
               this.renderPos.z + halfWidth
            );
            Render3D.boxSides(box, this.boxColor.getValue().toJavaColor());
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      Packet<?> packet = event.getPacket();
      if (!this.ignoreKeepAlive.getValue() || !(packet instanceof KeepAliveC2SPacket)) {
         if (event.getOrigin() == TransferOrigin.SEND) {
            if (this.shouldSkipForConsumable()) {
               return;
            }

            if (this.packets.isEmpty()) {
               this.packets.add(new LinkedList<>());
            }

            this.packets.getLast().add(packet);
            event.setCancelled(true);
            if (packet instanceof PlayerInteractEntityC2SPacket interact && interact.type == PlayerInteractEntityC2SPacket.ATTACK) {
               this.handleAttackRelease(interact);
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      this.ticks++;
      this.packets.add(new ArrayList<>());
      if (mc.player != null) {
         Vec3d pos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
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
            if (mc.world != null) {
               for (Entity entity : mc.world.getEntities()) {
                  if (entity instanceof ArrowEntity arrow && !arrow.isInGround() && this.isDangerous(arrow)) {
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

   private boolean isDangerous(Entity entity) {
      float width = 1.2F;
      float height = 2.2F;
      if (entity instanceof ProjectileEntity projectile) {
         float motionSlowdown = 0.99F;
         float size = 1.2F;
         float gravity = 0.05F;
         return ProjectionUtil.predictBox(
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            entity.getVelocity().x,
            entity.getVelocity().y,
            entity.getVelocity().z,
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

   private void sendTick(List<Packet<?>> tick) {
      if (Objects.requireNonNull(mc.getNetworkHandler()).getConnection() != null) {
         tick.forEach(PacketUtils::sendPacketNoEvent);
      }

      if (!this.realPos.isEmpty()) {
         this.realPos.remove(0);
      }
   }

   private void handleAttackRelease(PlayerInteractEntityC2SPacket interactPacket) {
      if (mc.world != null && !this.realPos.isEmpty()) {
         if (mc.world.getEntityById(interactPacket.entityId) instanceof PlayerEntity target) {
            Vec3d targetPos = target.getPos();
            Vec3d serverPos = this.getServerPosition();
            if (serverPos != null) {
               double desiredDistance = this.attackSyncDistanceValue.getValue().doubleValue();
               double currentDistance = serverPos.distanceTo(targetPos);

               while (currentDistance > desiredDistance && !this.packets.isEmpty()) {
                  this.poll();
                  serverPos = this.getServerPosition();
                  if (serverPos == null) {
                     break;
                  }

                  currentDistance = serverPos.distanceTo(targetPos);
               }
            }
         }
      }
   }

   private Vec3d getServerPosition() {
      return this.realPos.isEmpty() ? null : this.realPos.get(0);
   }

   private boolean shouldReleaseForSafety() {
      if (mc.player != null && mc.world != null) {
         Vec3d serverPos = this.getServerPosition();
         if (serverPos == null) {
            return false;
         } else {
            double range = this.dangerRangeValue.getValue().doubleValue();
            Vec3d delta = serverPos.subtract(mc.player.getPos());
            Box serverBox = mc.player.getBoundingBox().offset(delta);

            for (PlayerEntity entity : mc.world.getPlayers()) {
               if (entity != mc.player
                  && entity.isAlive()
                  && !entity.isSpectator()
                  && (this.includeTeamsValue.getValue() || !Teams.isTeam(entity))
                  && !(entity.squaredDistanceTo(mc.player) > range * range)) {
                  boolean intersects = serverBox.intersects(entity.getBoundingBox());
                  double serverDistance = entity.squaredDistanceTo(serverPos.x, serverPos.y, serverPos.z);
                  double clientDistance = entity.squaredDistanceTo(mc.player);
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
      if (!this.skipConsumablesValue.getValue() || mc.player == null) {
         return false;
      } else if (!mc.player.isUsingItem()) {
         return false;
      } else {
         ItemStack stack = mc.player.getActiveItem();
         return stack.isEmpty()
            ? false
            : ItemUtils.isConsumable(stack) || stack.getItem() == Items.MILK_BUCKET || stack.getItem() instanceof PotionItem;
      }
   }
}
