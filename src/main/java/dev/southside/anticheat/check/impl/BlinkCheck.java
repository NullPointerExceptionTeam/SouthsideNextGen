package dev.southside.anticheat.check.impl;

import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.player.PlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class BlinkCheck implements NormalCheck {
   private static final double FROZEN_THRESHOLD = 0.01;
   private static final int FREEZE_TICKS_MIN = 5;
   private static final double BURST_THRESHOLD = 1.5;
   private static final int COOLDOWN_TICKS = 30;

   @Override
   public void onUpdate(PlayerData playerData) {
      PlayerEntity player = playerData.getPlayer();
      int tick = player.age;
      Vec3d currentPos = player.getPos();
      Vec3d lastPos = playerData.getLastPos();
      if (lastPos == null) {
         playerData.setLastPos(currentPos);
      } else {
         double dx = currentPos.x - lastPos.x;
         double dz = currentPos.z - lastPos.z;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (dist < 0.01 && player.isAlive() && !player.hasVehicle()) {
            playerData.setBlinkFrozenTicks(playerData.getBlinkFrozenTicks() + 1);
         } else if (playerData.getBlinkFrozenTicks() >= 5) {
            if (dist > 1.5 && tick - playerData.getBlinkLastFlagTick() > 30) {
               float freezeFactor = Math.min(playerData.getBlinkFrozenTicks() / 10.0F, 3.0F);
               float distFactor = Math.min((float)(dist / 1.5), 3.0F);
               float vl = 1.0F + freezeFactor + distFactor;
               playerData.addVl(vl, "Blink");
               playerData.setBlinkLastFlagTick(tick);
               playerData.setBlinkBurstCount(0);
            }

            playerData.setBlinkFrozenTicks(0);
         } else {
            if (playerData.getBlinkFrozenTicks() >= 2 && dist > 0.5) {
               playerData.setBlinkBurstCount(playerData.getBlinkBurstCount() + 1);
               if (playerData.getBlinkBurstCount() >= 4 && tick - playerData.getBlinkLastFlagTick() > 30) {
                  playerData.addVl(2.0F, "Blink (Stutter)");
                  playerData.setBlinkLastFlagTick(tick);
                  playerData.setBlinkBurstCount(0);
               }
            } else if (dist > 0.01 && playerData.getBlinkBurstCount() > 0) {
               playerData.setBlinkBurstCount(playerData.getBlinkBurstCount() - 1);
            }

            playerData.setBlinkFrozenTicks(0);
         }

         playerData.setLastPos(currentPos);
      }
   }
}
