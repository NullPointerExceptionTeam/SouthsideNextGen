package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.PlayerUtils;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public class GrimFly extends Module {
   public int ticks = 0;
   private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
   private final Dropdown mode = new Dropdown("Mode", new String[]{"1平", "2随意", "3一直"}, "Mode", 1, 0);
   private final Switch velocityHud = new Switch("Velocity HUD", "Velocity HUD", true);
   private final Switch selectMaxVelocity = new Switch("Select Max Velocity", "Select Max Velocity", false);
   private boolean shouldEnabled;
   private boolean mode2HeightCaptured;
   private double mode2Height;
   private boolean mode2WasActive;
   private boolean mode2JumpWasPressed;
   private boolean mode2SneakWasPressed;
   private boolean mode2WaitingForApex;
   private double mode2MaxY;
   private double mode2MaxHorizontalSpeed;
   private boolean mode2HasValidY;

   public GrimFly() {
      super(Category.Movement, "GrimFly", "Grim CNMBBB");
   }

   @Override
   public void onEnable() {
      this.shouldEnabled = false;
      this.ticks = 0;
      this.mode2HeightCaptured = false;
      this.mode2Height = 0.0;
      this.mode2WasActive = false;
      this.mode2JumpWasPressed = false;
      this.mode2SneakWasPressed = false;
      this.mode2WaitingForApex = false;
      this.mode2MaxY = 0.0;
      this.mode2MaxHorizontalSpeed = 0.0;
      this.mode2HasValidY = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      if (!this.packets.isEmpty()) {
         this.releasePackets();
      }

      this.shouldEnabled = false;
      this.ticks = 0;
      this.mode2HeightCaptured = false;
      this.mode2Height = 0.0;
      this.mode2WasActive = false;
      this.mode2JumpWasPressed = false;
      this.mode2SneakWasPressed = false;
      this.mode2WaitingForApex = false;
      this.mode2MaxY = 0.0;
      this.mode2MaxHorizontalSpeed = 0.0;
      this.mode2HasValidY = false;
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.ticks = 0;
      this.mode2HeightCaptured = false;
      this.mode2Height = 0.0;
      this.mode2WasActive = false;
      this.mode2JumpWasPressed = false;
      this.mode2SneakWasPressed = false;
      this.mode2WaitingForApex = false;
      this.mode2MaxY = 0.0;
      this.mode2MaxHorizontalSpeed = 0.0;
      this.mode2HasValidY = false;
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (mc.player != null && event.getState() != Stage.POST) {
         boolean mode2Active = this.mode.isMode("2随意");
         if (mode2Active && !this.mode2WasActive) {
            this.mode2HeightCaptured = false;
            this.mode2Height = 0.0;
            this.mode2JumpWasPressed = false;
            this.mode2SneakWasPressed = false;
            this.mode2WaitingForApex = false;
            this.mode2MaxY = 0.0;
            this.mode2MaxHorizontalSpeed = 0.0;
            this.mode2HasValidY = false;
         }

         String var3 = this.mode.getMode();
         switch (var3) {
            case "1平":
               if (Math.abs(mc.player.getY() % 1.0 - 0.24919) < 1.0E-4) {
                  this.shouldEnabled = Math.abs(mc.player.getY() % 1.0 - 0.24919) < 0.001;
               }
               break;
            case "2随意":
               boolean jumpPressed = mc.options.jumpKey.isPressed();
               boolean sneakPressed = mc.options.sneakKey.isPressed();
               if (this.selectMaxVelocity.getValue()) {
                  double minYVel = 0.4;
                  if (jumpPressed && !this.mode2JumpWasPressed) {
                     this.mode2MaxY = mc.player.getY();
                     this.mode2MaxHorizontalSpeed = 0.0;
                     this.mode2HasValidY = false;
                  }

                  if (jumpPressed) {
                     Vec3d velocity = mc.player.getVelocity();
                     if (!this.mode2HasValidY && Math.abs(velocity.y) >= minYVel) {
                        this.mode2HasValidY = true;
                     }

                     if (this.mode2HasValidY) {
                        double horizontalSpeed = Math.hypot(velocity.x, velocity.z);
                        if (horizontalSpeed > this.mode2MaxHorizontalSpeed) {
                           this.mode2MaxHorizontalSpeed = horizontalSpeed;
                        }

                        if (mc.player.getY() > this.mode2MaxY) {
                           this.mode2MaxY = mc.player.getY();
                        }

                        if (mc.player.getY() > this.mode2Height) {
                           this.mode2Height = mc.player.getY();
                           this.mode2HeightCaptured = true;
                        }
                     }
                  }

                  if (!jumpPressed && this.mode2JumpWasPressed && this.mode2HasValidY) {
                     this.mode2WaitingForApex = true;
                  }

                  if (this.mode2WaitingForApex) {
                     if (mc.player.getY() > this.mode2MaxY) {
                        this.mode2MaxY = mc.player.getY();
                     }

                     if (mc.player.getVelocity().y <= 0.0) {
                        this.mode2Height = this.mode2MaxY;
                        this.mode2HeightCaptured = true;
                        this.mode2WaitingForApex = false;
                     }
                  }
               } else {
                  if (jumpPressed && mc.player.getY() > this.mode2Height) {
                     this.mode2Height = mc.player.getY();
                     this.mode2HeightCaptured = true;
                  }

                  if (!jumpPressed && this.mode2JumpWasPressed) {
                     this.mode2Height = mc.player.getY();
                     this.mode2HeightCaptured = true;
                  }
               }

               if (!sneakPressed && this.mode2SneakWasPressed) {
                  this.mode2Height = mc.player.getY();
                  this.mode2HeightCaptured = true;
                  this.mode2WaitingForApex = false;
               }

               boolean heightReady = this.mode2HeightCaptured && !this.mode2WaitingForApex;
               if (jumpPressed) {
                  this.shouldEnabled = true;
               } else {
                  this.shouldEnabled = heightReady && mc.player.getY() <= this.mode2Height;
               }

               this.mode2JumpWasPressed = jumpPressed;
               this.mode2SneakWasPressed = sneakPressed;
               break;
            case "3一直":
               this.shouldEnabled = true;
         }

         if (mc.options.sneakKey.isPressed()) {
            this.shouldEnabled = false;
         }

         this.mode2WasActive = mode2Active;
         if (++this.ticks % 2 == 0 && this.shouldEnabled) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
         }
      }
   }

   private void releasePackets() {
      while (!this.packets.isEmpty()) {
         Packet<?> packet = this.packets.poll();
         if (!ServerPacketManager.onPacket(packet)) {
            PacketUtils.handlePacket(packet);
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.player != null) {
         if (event.getOrigin() == TransferOrigin.RECEIVE) {
            Packet<?> packet = event.getPacket();
            if (event.isCancelled()) {
               return;
            }

            if (event.getPacket() instanceof CommonPingS2CPacket) {
               event.setCancelled(true);
               this.packets.add(packet);
            }

            if (event.getPacket() instanceof PlayerPositionLookS2CPacket pplPacket) {
               if (this.mode.isMode("2随意") && !this.mode2HeightCaptured) {
                  PlayerPosition change = PlayerUtils.resolvePos(pplPacket.change(), pplPacket.relatives());
                  this.mode2Height = change.position().y;
                  this.mode2HeightCaptured = true;
               }

               this.releasePackets();
            }
         }
      }
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (this.isEnabled() && mc.player != null && this.velocityHud.getValue()) {
         Vec3d velocity = mc.player.getVelocity();
         double speed = velocity.length();
         String text = String.format(Locale.US, "x: %.3f y: %.3f z: %.3f", velocity.x, velocity.y, velocity.z);
         int x = (mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(text)) / 2;
         int y = mc.getWindow().getScaledHeight() / 2 + 30;
         event.getDrawContext().drawText(mc.textRenderer, text, x, y, 16777215, true);
      }
   }
}
