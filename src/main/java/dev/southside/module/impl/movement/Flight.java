package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MoveEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.BlinkUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.FakePlayerEntity;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.world.EntityUtils;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public class Flight extends Module {
   public final Dropdown mode = new Dropdown("Mode", "Vanilla", "Vanilla", "Cubecraft", "CubeZoom");
   public Slider hSpeed = new Slider("Horizontal", 1.0F, 0.0F, 10.0F);
   public Slider vSpeed = new Slider("Vertical", 0.78F, 0.0F, 5.0F);
   private final Slider cubeSpeed = new Slider("Speed", 0.33, 0.1, 0.34, 0.01);
   private final Slider blinkDelay = new Slider("Release Delay", 300, 100, 2000, 50);
   private final Switch viewBobbing = new Switch("View Bobbing", true);
   private final MSTimer blinkTimer = new MSTimer();
   private final MSTimer hoverTimer = new MSTimer();
   private boolean cancelTick = false;
   private boolean isBlinking = false;
   public FakePlayerEntity fakePlayer;

   public Flight() {
      super(Category.Movement, "Flight");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.hSpeed.getLabel(), () -> this.mode.isMode("Cubecraft"));
      this.hideIf(this.vSpeed.getLabel(), () -> this.mode.isMode("Cubecraft"));
      this.hideIf(this.cubeSpeed.getLabel(), () -> !this.mode.isMode("Cubecraft"));
      this.hideIf(this.blinkDelay.getLabel(), () -> !this.mode.isMode("CubeZoom"));
      this.hideIf(this.viewBobbing.getLabel(), () -> !this.mode.isMode("CubeZoom"));
   }

   @Override
   public void onEnable() {
      this.cancelTick = false;
      if (this.mode.isMode("CubeZoom")) {
         if (mc.player != null && mc.world != null) {
            this.fakePlayer = new FakePlayerEntity(mc.player, mc.player.getGameProfile().getName(), 20.0F, true);
            this.fakePlayer.doNotPush = true;
            this.fakePlayer.hideWhenInsideCamera = true;
            this.fakePlayer.spawn();
         }

         BlinkUtils.blink();
         this.isBlinking = true;
         this.blinkTimer.reset();
      }
   }

   @Override
   public void onDisable() {
      this.cancelTick = false;
      if (this.isBlinking) {
         BlinkUtils.stopBlink();
         BlinkUtils.releasePacket();
         this.isBlinking = false;
      }

      if (this.fakePlayer != null) {
         if (mc.world != null) {
            mc.world.removeEntity(this.fakePlayer.getId(), RemovalReason.DISCARDED);
         }

         this.fakePlayer = null;
      }
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.cancelTick = false;
      if (this.fakePlayer != null) {
         if (mc.world != null) {
            mc.world.removeEntity(this.fakePlayer.getId(), RemovalReason.DISCARDED);
         }

         this.fakePlayer = null;
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.player != null) {
         Packet<?> packet = event.getPacket();
         String var3 = this.mode.getMode();
         switch (var3) {
            case "CubeZoom":
               if (this.blinkTimer.hasPassed(this.blinkDelay.getValue().intValue()) && this.fakePlayer != null) {
                  this.handleFakePlayerPacket(packet);
               }
            case "Vanilla":
         }
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      this.setSuffix(this.mode.getMode());
      if (mc.player != null) {
         if (this.mode.isMode("Vanilla")) {
            if (PlayerUtils.isMoving()) {
               double[] dir = PlayerUtils.forward(this.hSpeed.getValue().floatValue());
               mc.player.setVelocity(dir[0], 0.0, dir[1]);
            } else {
               mc.player.setVelocity(0.0, 0.0, 0.0);
            }

            if (mc.options.jumpKey.isPressed()) {
               mc.player.setVelocity(mc.player.getVelocity().add(0.0, this.vSpeed.getValue().floatValue(), 0.0));
            }

            if (mc.options.sneakKey.isPressed()) {
               mc.player.setVelocity(mc.player.getVelocity().add(0.0, -this.vSpeed.getValue().floatValue(), 0.0));
            }
         } else if (this.mode.isMode("CubeZoom")) {
            if (PlayerUtils.isMoving()) {
               double[] dir = PlayerUtils.forward(this.hSpeed.getValue().floatValue());
               mc.player.setVelocity(dir[0], 0.0, dir[1]);
               if (mc.options.jumpKey.isPressed()) {
                  mc.player.setVelocity(mc.player.getVelocity().add(0.0, this.vSpeed.getValue().floatValue(), 0.0));
               }

               if (mc.options.sneakKey.isPressed()) {
                  mc.player.setVelocity(mc.player.getVelocity().add(0.0, -this.vSpeed.getValue().floatValue(), 0.0));
               }

               if (this.blinkTimer.hasPassed(this.blinkDelay.getValue().intValue())) {
                  BlinkUtils.releasePacket();
                  BlinkUtils.blink();
                  this.blinkTimer.reset();
                  if (this.fakePlayer != null) {
                     this.fakePlayer.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                     this.fakePlayer.setYaw(mc.player.getYaw());
                     this.fakePlayer.setPitch(mc.player.getPitch());
                  }
               }
            } else {
               BlinkUtils.releasePacket();
               BlinkUtils.stopBlink();
               this.isBlinking = false;
            }
         }
      }
   }

   @EventHandler
   public void onMove(MoveEvent event) {
      if (this.mode.isMode("Cubecraft")) {
         Vec3d vec3d = EntityUtils.withStrafe(new Vec3d(event.getX(), event.getY(), event.getZ()), 1.0);
         event.setX(vec3d.x);
         event.setY(vec3d.y);
         event.setZ(vec3d.z);
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.player != null) {
         String var2 = this.mode.getMode();
         switch (var2) {
            case "CubeZoom":
               if (mc.isInSingleplayer()) {
                  this.setEnable(false);
               }

               if (this.viewBobbing.getValue()) {
               }

               if (mc.player.isOnGround() || PlayerUtils.isMoving()) {
                  this.hoverTimer.reset();
               } else if (this.hoverTimer.hasPassed(3000L)) {
                  ChatUtils.info("想死号?!");
                  this.setEnable(false);
               }
               break;
            case "Cubecraft":
               if (mc.player.isOnGround()) {
                  return;
               }

               if (mc.player.isSneaking()) {
                  mc.player.setVelocity(mc.player.getVelocity().x, -0.4, mc.player.getVelocity().z);
               } else if (mc.player.input.playerInput.jump()) {
                  mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
               } else {
                  mc.player.setVelocity(mc.player.getVelocity().x, 0.2, mc.player.getVelocity().z);
               }

               mc.player
                  .setVelocity(EntityUtils.withStrafe(mc.player.getVelocity(), RandomUtils.random(this.cubeSpeed.getMin(), this.cubeSpeed.getMax())));
               if (!this.cancelTick) {
                  PlayerUtils.setSkipTicks(6);
                  this.cancelTick = true;
               }
         }
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
      } else if (packet instanceof HandSwingC2SPacket handSwingC2SPacket) {
         this.fakePlayer.swingHand(handSwingC2SPacket.getHand());
      }
   }
}
