package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PostStrafeEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.MinecraftTickTimerUtils;
import dev.southside.utils.network.PacketUtils;
import lombok.Generated;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2743;
import net.minecraft.class_2828;
import net.minecraft.class_2828.class_5911;

public class LongJump2 extends Module {
   private int jumpStage;
   private int motionStage;
   private int packetCounter;
   private int jumpTimer;
   private int boostCounter;
   private int state;
   private double boostPower;
   public final Slider timer = new Slider("Timer", 2.0F, 1.0F, 3.0F, 0.05);

   public LongJump2() {
      super(Category.Movement, "LongJump2");
   }

   @EventHandler
   public void onPreMotion(MotionEvent event) {
      if (!event.getState().equals(Stage.POST)) {
         MinecraftTickTimerUtils.setMultiplier(this.timer.getValue().floatValue());
         event.setPitch(event.getPitch() + (float)(Math.random() * 0.1));
         if (this.jumpTimer == 0) {
         }

         mc.field_1724.method_18799(mc.field_1724.method_18798().method_1031(0.0, 0.42, 0.0));
         if (this.state == 1) {
            this.state = 2;
            this.motionStage = 0;
         } else if (this.state == 3) {
            mc.field_1724.method_18799(mc.field_1724.method_18798().method_1021(0.0));
            this.packetCounter = 1;
            this.motionStage++;
            if (this.motionStage > 20) {
               this.toggle();
            }
         }
      }
   }

   @EventHandler
   public void onPostStrafe(PostStrafeEvent event) {
      if (this.boostCounter == 0) {
         mc.field_1724.method_6043();
      }

      if (this.boostCounter == 1) {
         this.jumpStage = 1;

         for (int i = 0; i < 10; i++) {
            PacketUtils.sendPacket(new class_5911(true, mc.field_1724.field_5976));
         }

         ChatUtils.info("321");
         this.packetCounter = 1;
         this.state = 3;
      }

      this.boostCounter++;
   }

   @EventHandler
   public void onPacketReceive(PacketEvent event) {
      if (event.getPacket() instanceof class_2828) {
         if (this.packetCounter != 1) {
            return;
         }

         this.packetCounter = 0;
         event.setCancelled(true);
      }

      if (!event.getOrigin().equals(TransferOrigin.SEND)) {
         class_2596<?> packet = event.getPacket();
         if (packet instanceof class_2743 velocityPacket
            && velocityPacket.method_11818() == mc.field_1724.method_5628()
            && velocityPacket.method_11816() / 8000.0 < 0.0) {
            this.toggle();
         }

         if (packet instanceof class_2708) {
            this.state = 1;
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.resetState();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      MinecraftTickTimerUtils.setMultiplier(1.0F);
      this.resetState();
   }

   public void resetState() {
      this.jumpStage = 0;
      this.motionStage = 0;
      this.packetCounter = 0;
      this.jumpTimer = 0;
      this.boostCounter = 0;
      this.state = 0;
      this.boostPower = 0.0;
   }

   public boolean isWorking() {
      return this.state > 0 && this.state < 4;
   }

   @Generated
   public int getState() {
      return this.state;
   }

   @Generated
   public void setBoostPower(double boostPower) {
      this.boostPower = boostPower;
   }

   @Generated
   public double getBoostPower() {
      return this.boostPower;
   }
}
