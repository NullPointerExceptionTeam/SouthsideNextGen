package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.FakePlayerEntity;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.world.GameMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public class Blink extends Module {
   private LinkedList<List<Packet<?>>> packets = new LinkedList<>();
   public FakePlayerEntity fakePlayer;
   public final Slider maxPacketsValue = new Slider("Max Packets", 1, 1, 200, 1);
   public static final Switch slowReleaseValue = new Switch("Slow Release", false);
   public final Switch ignoreKeepAlive = new Switch("Ignore KeepAlive", false);
   public final Switch antiAimValue = new Switch("Anti Aim", true);
   public final Switch antiAimArrowValue = new Switch("Arrow", true);
   public final Switch antiAimProjectileValue = new Switch("Projectile", true);
   public final Switch antiAimTNTValue = new Switch("TNT", true);
   public final Switch antiAimPlayerValue = new Switch("Player", true);
   private int ticks;
   private List<Vec3d> realPos = new ArrayList<>();

   public Blink() {
      super(Category.Player, "Blink", "666开瞬移了。。");
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.world != null) {
         this.packets.clear();
         this.packets.add(new ArrayList<>());
         mc.player.getAbilities().allowModifyWorld = true;
         mc.player.onGameModeChanged(GameMode.SURVIVAL);
         this.fakePlayer = new FakePlayerEntity(mc.player, mc.player.getGameProfile().getName(), 20.0F, true);
         this.fakePlayer.doNotPush = true;
         this.fakePlayer.hideWhenInsideCamera = true;
         this.fakePlayer.spawn();
         this.ticks = 0;
         this.realPos.add(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
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
      if (this.fakePlayer != null) {
         if (mc.world != null) {
            mc.world.removeEntity(this.fakePlayer.getId(), RemovalReason.DISCARDED);
         }

         this.fakePlayer = null;
      }

      super.onDisable();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.setEnable(false);
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      Packet<?> packet = event.getPacket();
      if (!this.ignoreKeepAlive.getValue() || !(packet instanceof KeepAliveC2SPacket)) {
         if (event.getOrigin() == TransferOrigin.SEND) {
            if (this.packets.isEmpty()) {
               this.packets.add(new LinkedList<>());
            }

            this.packets.getLast().add(packet);
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      this.ticks++;
      this.packets.add(new ArrayList<>());
      if (mc.player != null) {
         this.realPos.add(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
      }

      if (this.packets.size() >= this.maxPacketsValue.getValue().intValue() || this.ticks % 10 == 0 && slowReleaseValue.getValue()) {
         this.poll();
      }

      if (this.antiAimValue.getValue()) {
         while (true) {
            boolean dangerous = false;
            if (mc.world != null) {
               for (Entity entity : mc.world.getEntities()) {
                  if (mc.player != null
                     && (
                        this.antiAimArrowValue.getValue() && entity instanceof ArrowEntity arrow && !arrow.isInGround()
                           || this.antiAimProjectileValue.getValue() && entity instanceof SnowballEntity
                           || entity instanceof EggEntity
                           || this.antiAimTNTValue.getValue() && entity instanceof TntEntity
                           || this.antiAimPlayerValue.getValue()
                              && entity instanceof PlayerEntity player
                              && this.fakePlayer != null
                              && entity.getId() != this.fakePlayer.getId()
                              && player.getId() != mc.player.getId()
                     )
                     && this.isDangerous(entity)) {
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
   }

   private boolean isDangerous(Entity entity) {
      float width = 1.2F;
      float height = 2.2F;
      if (entity instanceof ProjectileEntity projectile) {
         float motionSlowdown = 0.99F;
         float size = 1.2F;
         float gravity = 0.05F;
         if (!(projectile instanceof ArrowEntity) && (projectile instanceof SnowballEntity || projectile instanceof EggEntity)) {
            gravity = 1.2F;
            size = 0.25F;
         }

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
      } else if (entity instanceof TntEntity tnt) {
         Vec3d pos = this.realPos.get(0);
         return tnt.squaredDistanceTo(pos.x, pos.y, pos.z) <= 30.0 && tnt.getFuse() <= 10;
      } else if (!(entity instanceof PlayerEntity player)) {
         return false;
      } else {
         Vec3d pos = this.realPos.get(0);
         return player.squaredDistanceTo(pos.x, pos.y, pos.z) <= 25.0 && player.getUuid() != mc.player.getUuid();
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
         tick.forEach(packet -> {
            PacketUtils.sendPacketNoEvent((Packet<?>)packet);
            this.handleFakePlayerPacket((Packet<?>)packet);
         });
      }

      if (!this.realPos.isEmpty()) {
         this.realPos.remove(0);
      }
   }

   private void handleFakePlayerPacket(Packet<?> packet) {
      if (packet instanceof PositionAndOnGround position) {
         this.fakePlayer.setPosition(position.x, position.y, position.z);
         this.fakePlayer.setYaw(this.fakePlayer.getYaw());
         this.fakePlayer.setPitch(this.fakePlayer.getPitch());
         this.fakePlayer.setOnGround(position.isOnGround());
      } else if (packet instanceof LookAndOnGround rotation) {
         this.fakePlayer.setPosition(this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ());
         this.fakePlayer.setHeadYaw(rotation.getYaw(this.fakePlayer.getYaw()));
         this.fakePlayer.setPitch(rotation.getPitch(this.fakePlayer.getPitch()));
         this.fakePlayer.setOnGround(rotation.isOnGround());
         this.fakePlayer.setHeadYaw(rotation.getYaw(this.fakePlayer.getYaw()));
         this.fakePlayer.setYaw(rotation.getYaw(this.fakePlayer.getYaw()));
         this.fakePlayer.setPitch(rotation.pitch);
      } else if (packet instanceof Full positionRotation) {
         this.fakePlayer.setPosition(positionRotation.x, positionRotation.y, positionRotation.z);
         this.fakePlayer.setYaw(positionRotation.yaw);
         this.fakePlayer.setPitch(positionRotation.pitch);
         this.fakePlayer.setOnGround(positionRotation.isOnGround());
         this.fakePlayer.setHeadYaw(positionRotation.getYaw(this.fakePlayer.getYaw()));
         this.fakePlayer.setYaw(positionRotation.getYaw(this.fakePlayer.getYaw()));
         this.fakePlayer.setPitch(positionRotation.pitch);
      } else if (packet instanceof ClientCommandC2SPacket action) {
         if (action.getMode() == Mode.START_SPRINTING) {
            this.fakePlayer.setSprinting(true);
         } else if (action.getMode() == Mode.STOP_SPRINTING) {
            this.fakePlayer.setSprinting(false);
         } else if (action.getMode() == Mode.PRESS_SHIFT_KEY) {
            this.fakePlayer.setSneaking(true);
         } else if (action.getMode() == Mode.RELEASE_SHIFT_KEY) {
            this.fakePlayer.setSneaking(false);
         }
      } else if (packet instanceof HandSwingC2SPacket CNM) {
         this.fakePlayer.swingHand(CNM.getHand());
      }
   }

   public static int getReleaseSpeedModifier(int ticks, int length) {
      if (length >= 195) {
         return 1;
      } else {
         return slowReleaseValue.getValue() ? (ticks % 10 == 0 ? 1 : 0) : 0;
      }
   }
}
