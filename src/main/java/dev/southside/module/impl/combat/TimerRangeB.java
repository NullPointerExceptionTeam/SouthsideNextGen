package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.MinecraftTickTimerUtils;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.player.TargetUtils;
import net.minecraft.class_1657;
import net.minecraft.class_2743;
import net.minecraft.class_2824;

public class TimerRangeB extends Module {
   private final Slider range = new Slider("Range", 3.5, 1.0, 6.0, 0.02);
   private final Slider positiveTimer = new Slider("PositiveTimer", 1.5, 0.1, 20.0, 0.05);
   private final Slider positiveTicks = new Slider("PositiveTicks", 8, 1, 30, 1);
   private final Slider negativeTimer = new Slider("NegativeTimer", 0.4, 0.05, 5.0, 0.05);
   private final Slider negativeTicks = new Slider("NegativeTicks", 15, 1, 50, 1);
   private final Switch confirmKB = new Switch("ConfirmKnockback", true);
   private int playerTicks = 0;
   private int cooldownTick = 0;
   private boolean confirmAttack = false;
   private boolean confirmKnockback = false;

   public TimerRangeB() {
      super(Category.Combat, "TimerRangeB", "Adaptive timer based on enemy distance");
   }

   @EventHandler
   public void onWorld(WorldEvent e) {
      this.resetTimer();
      this.cooldownTick = 0;
      this.playerTicks = 0;
      this.confirmAttack = false;
      this.confirmKnockback = false;
   }

   @Override
   public void onEnable() {
      this.resetTimer();
      this.cooldownTick = 0;
      this.playerTicks = 0;
      this.confirmAttack = false;
      this.confirmKnockback = false;
   }

   @Override
   public void onDisable() {
      this.resetTimer();
      this.playerTicks = 0;
      this.cooldownTick = 0;
      this.confirmAttack = false;
      this.confirmKnockback = false;
   }

   @EventHandler
   public void onPlayerTick(TickEvent event) {
      if (event.getState() != Stage.POST) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            double timerboost = MathUtils.randomDouble(0.5, 0.5);
            double charged = MathUtils.randomDouble(0.9, 0.9);
            if (this.playerTicks <= 0) {
               this.resetTimer();
            } else {
               double tickProgress = this.playerTicks / this.positiveTicks.getValue().floatValue();
               float playerSpeed;
               if (tickProgress < timerboost) {
                  playerSpeed = this.positiveTimer.getValue().floatValue();
               } else if (tickProgress < charged) {
                  playerSpeed = this.negativeTimer.getValue().floatValue();
               } else {
                  playerSpeed = 1.0F;
               }

               float speedAdjustment = playerSpeed >= 0.0F ? playerSpeed : 1.0F + this.positiveTicks.getValue().floatValue() - this.playerTicks;
               float adjustedTimerSpeed = Math.max(speedAdjustment, 0.0F);
               MinecraftTickTimerUtils.setMultiplier(adjustedTimerSpeed);
               this.playerTicks--;
            }
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (PlayerUtils.isMoving()
            && !this.shouldResetTimer()
            && (mc.field_52750.field_51964 > 50.0 || mc.field_52750.field_51964 < 50.0)
            && event.getOrigin() == TransferOrigin.RECEIVE
            && event.getPacket() instanceof class_2743 p
            && mc.field_1724.method_5628() == p.method_11818()
            && p.method_11816() > 0.0
            && (p.method_11815() != 0.0 || p.method_11819() != 0.0)) {
            this.confirmKnockback = false;
            this.resetTimer();
         }

         if (event.isSend() && event.getPacket() instanceof class_2824 p) {
            if (p.field_12871 != class_2824.field_29170 && this.playerTicks >= 1) {
               this.resetTimer();
               return;
            }

            this.confirmAttack = true;
            double entityDistance = mc.field_1724.method_5739(mc.field_1687.method_8469(p.field_12870));
            this.cooldownTick++;
            boolean shouldSlowed = this.cooldownTick >= this.negativeTicks.getValue().intValue() && entityDistance > this.range.getValue().floatValue();
            if (shouldSlowed && this.confirmAttack) {
               this.confirmAttack = false;
               this.playerTicks = this.positiveTicks.getValue().intValue();
               this.confirmKnockback = true;
               this.cooldownTick = 0;
            } else {
               this.resetTimer();
            }
         }
      }
   }

   private class_1657 searchTargets() {
      class_1657 closest = null;
      double closestDist = Double.MAX_VALUE;

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && TargetUtils.isTarget(player, true, true)) {
            double dist = mc.field_1724.method_5739(player);
            if (dist <= this.range.getValue().floatValue() && dist < closestDist) {
               closest = player;
               closestDist = dist;
            }
         }
      }

      return closest;
   }

   private boolean shouldResetTimer() {
      return this.playerTicks >= 1
         || mc.field_1724.method_7325()
         || mc.field_1724.method_29504()
         || mc.field_1724.method_5771()
         || mc.field_1724.method_52535()
         || mc.field_1724.method_21754()
         || mc.field_1724.method_3144();
   }

   private double getDistancePrediction() {
      double predictX = mc.field_1724.method_23317() + (mc.field_1724.method_23317() - mc.field_1724.field_3926) * 2.0;
      double predictZ = mc.field_1724.method_23321() + (mc.field_1724.method_23321() - mc.field_1724.field_3924) * 2.0;
      float f = (float)(predictX - mc.field_1724.method_23317());
      float f1 = (float)(mc.field_1724.method_23318() - mc.field_1724.method_23318());
      float f2 = (float)(predictZ - mc.field_1724.method_23321());
      return MathUtils.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   private void resetTimer() {
      MinecraftTickTimerUtils.setMultiplier(1.0F);
   }
}
