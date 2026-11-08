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
import net.minecraft.class_10182;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2848;
import net.minecraft.class_6373;
import net.minecraft.class_2848.class_2849;

public class GrimFly extends Module {
   public int ticks = 0;
   private final LinkedBlockingQueue<class_2596<?>> packets = new LinkedBlockingQueue<>();
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
      if (mc.field_1724 != null && event.getState() != Stage.POST) {
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
               if (Math.abs(mc.field_1724.method_23318() % 1.0 - 0.24919) < 1.0E-4) {
                  this.shouldEnabled = Math.abs(mc.field_1724.method_23318() % 1.0 - 0.24919) < 0.001;
               }
               break;
            case "2随意":
               boolean jumpPressed = mc.field_1690.field_1903.method_1434();
               boolean sneakPressed = mc.field_1690.field_1832.method_1434();
               if (this.selectMaxVelocity.getValue()) {
                  double minYVel = 0.4;
                  if (jumpPressed && !this.mode2JumpWasPressed) {
                     this.mode2MaxY = mc.field_1724.method_23318();
                     this.mode2MaxHorizontalSpeed = 0.0;
                     this.mode2HasValidY = false;
                  }

                  if (jumpPressed) {
                     class_243 velocity = mc.field_1724.method_18798();
                     if (!this.mode2HasValidY && Math.abs(velocity.field_1351) >= minYVel) {
                        this.mode2HasValidY = true;
                     }

                     if (this.mode2HasValidY) {
                        double horizontalSpeed = Math.hypot(velocity.field_1352, velocity.field_1350);
                        if (horizontalSpeed > this.mode2MaxHorizontalSpeed) {
                           this.mode2MaxHorizontalSpeed = horizontalSpeed;
                        }

                        if (mc.field_1724.method_23318() > this.mode2MaxY) {
                           this.mode2MaxY = mc.field_1724.method_23318();
                        }

                        if (mc.field_1724.method_23318() > this.mode2Height) {
                           this.mode2Height = mc.field_1724.method_23318();
                           this.mode2HeightCaptured = true;
                        }
                     }
                  }

                  if (!jumpPressed && this.mode2JumpWasPressed && this.mode2HasValidY) {
                     this.mode2WaitingForApex = true;
                  }

                  if (this.mode2WaitingForApex) {
                     if (mc.field_1724.method_23318() > this.mode2MaxY) {
                        this.mode2MaxY = mc.field_1724.method_23318();
                     }

                     if (mc.field_1724.method_18798().field_1351 <= 0.0) {
                        this.mode2Height = this.mode2MaxY;
                        this.mode2HeightCaptured = true;
                        this.mode2WaitingForApex = false;
                     }
                  }
               } else {
                  if (jumpPressed && mc.field_1724.method_23318() > this.mode2Height) {
                     this.mode2Height = mc.field_1724.method_23318();
                     this.mode2HeightCaptured = true;
                  }

                  if (!jumpPressed && this.mode2JumpWasPressed) {
                     this.mode2Height = mc.field_1724.method_23318();
                     this.mode2HeightCaptured = true;
                  }
               }

               if (!sneakPressed && this.mode2SneakWasPressed) {
                  this.mode2Height = mc.field_1724.method_23318();
                  this.mode2HeightCaptured = true;
                  this.mode2WaitingForApex = false;
               }

               boolean heightReady = this.mode2HeightCaptured && !this.mode2WaitingForApex;
               if (jumpPressed) {
                  this.shouldEnabled = true;
               } else {
                  this.shouldEnabled = heightReady && mc.field_1724.method_23318() <= this.mode2Height;
               }

               this.mode2JumpWasPressed = jumpPressed;
               this.mode2SneakWasPressed = sneakPressed;
               break;
            case "3一直":
               this.shouldEnabled = true;
         }

         if (mc.field_1690.field_1832.method_1434()) {
            this.shouldEnabled = false;
         }

         this.mode2WasActive = mode2Active;
         if (++this.ticks % 2 == 0 && this.shouldEnabled) {
            mc.method_1562().method_52787(new class_2848(mc.field_1724, class_2849.field_12982));
         }
      }
   }

   private void releasePackets() {
      while (!this.packets.isEmpty()) {
         class_2596<?> packet = this.packets.poll();
         if (!ServerPacketManager.onPacket(packet)) {
            PacketUtils.handlePacket(packet);
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.field_1724 != null) {
         if (event.getOrigin() == TransferOrigin.RECEIVE) {
            class_2596<?> packet = event.getPacket();
            if (event.isCancelled()) {
               return;
            }

            if (event.getPacket() instanceof class_6373) {
               event.setCancelled(true);
               this.packets.add(packet);
            }

            if (event.getPacket() instanceof class_2708 pplPacket) {
               if (this.mode.isMode("2随意") && !this.mode2HeightCaptured) {
                  class_10182 change = PlayerUtils.resolvePos(pplPacket.comp_3228(), pplPacket.comp_3229());
                  this.mode2Height = change.comp_3148().field_1351;
                  this.mode2HeightCaptured = true;
               }

               this.releasePackets();
            }
         }
      }
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      if (this.isEnabled() && mc.field_1724 != null && this.velocityHud.getValue()) {
         class_243 velocity = mc.field_1724.method_18798();
         double speed = velocity.method_1033();
         String text = String.format(Locale.US, "x: %.3f y: %.3f z: %.3f", velocity.field_1352, velocity.field_1351, velocity.field_1350);
         int x = (mc.method_22683().method_4486() - mc.field_1772.method_1727(text)) / 2;
         int y = mc.method_22683().method_4502() / 2 + 30;
         event.getDrawContext().method_51433(mc.field_1772, text, x, y, 16777215, true);
      }
   }
}
