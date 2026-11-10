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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

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
         if (mc.world != null && mc.player != null) {
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
      if (mc.player != null && mc.world != null) {
         if (PlayerUtils.isMoving()
            && !this.shouldResetTimer()
            && (mc.renderTickCounter.tickTime > 50.0 || mc.renderTickCounter.tickTime < 50.0)
            && event.getOrigin() == TransferOrigin.RECEIVE
            && event.getPacket() instanceof EntityVelocityUpdateS2CPacket p
            && mc.player.getId() == p.getEntityId()
            && p.getVelocityY() > 0.0
            && (p.getVelocityX() != 0.0 || p.getVelocityZ() != 0.0)) {
            this.confirmKnockback = false;
            this.resetTimer();
         }

         if (event.isSend() && event.getPacket() instanceof PlayerInteractEntityC2SPacket p) {
            if (p.type != PlayerInteractEntityC2SPacket.ATTACK && this.playerTicks >= 1) {
               this.resetTimer();
               return;
            }

            this.confirmAttack = true;
            double entityDistance = mc.player.distanceTo(mc.world.getEntityById(p.entityId));
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

   private PlayerEntity searchTargets() {
      PlayerEntity closest = null;
      double closestDist = Double.MAX_VALUE;

      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != mc.player && TargetUtils.isTarget(player, true, true)) {
            double dist = mc.player.distanceTo(player);
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
         || mc.player.isSpectator()
         || mc.player.isDead()
         || mc.player.isInLava()
         || mc.player.isInFluid()
         || mc.player.isHoldingOntoLadder()
         || mc.player.isRiding();
   }

   private double getDistancePrediction() {
      double predictX = mc.player.getX() + (mc.player.getX() - mc.player.lastX) * 2.0;
      double predictZ = mc.player.getZ() + (mc.player.getZ() - mc.player.lastZ) * 2.0;
      float f = (float)(predictX - mc.player.getX());
      float f1 = (float)(mc.player.getY() - mc.player.getY());
      float f2 = (float)(predictZ - mc.player.getZ());
      return MathUtils.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   private void resetTimer() {
      MinecraftTickTimerUtils.setMultiplier(1.0F);
   }
}
