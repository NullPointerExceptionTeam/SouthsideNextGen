package dev.southside.anticheat.check.impl;

import dev.southside.MinecraftAccessor;
import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.player.PlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class FastWebCheck implements NormalCheck {
   private static final double SPEED_THRESHOLD_PER_TICK = 0.04;
   private static final int FLAG_COOLDOWN_TICKS = 20;

   @Override
   public void onUpdate(PlayerData playerData) {
      PlayerEntity player = playerData.getPlayer();
      int tick = player.age;
      if (MinecraftAccessor.mc.world != null && player.isAlive()) {
         boolean nearCobweb = this.isNearCobweb(player);
         if (nearCobweb) {
            double vx = player.getVelocity().x;
            double vz = player.getVelocity().z;
            double speed = Math.sqrt(vx * vx + vz * vz);
            if (speed > 0.04) {
               playerData.setFastWebTicks(playerData.getFastWebTicks() + 1);
               if (playerData.getFastWebTicks() >= 5) {
                  boolean outOfCooldown = tick - playerData.getFastWebLastFlagTick() > 20;
                  if (outOfCooldown) {
                     float vl = Math.min((float)(speed / 0.04), 5.0F);
                     playerData.addVl(vl, "FastWeb");
                     playerData.setFastWebLastFlagTick(tick);
                  }

                  playerData.setFastWebTicks(0);
               }
            }
         } else if (playerData.getFastWebTicks() > 0) {
            playerData.setFastWebTicks(playerData.getFastWebTicks() - 1);
         }
      }
   }

   private boolean isNearCobweb(PlayerEntity player) {
      BlockPos center = player.getBlockPos();

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (MinecraftAccessor.mc.world.getBlockState(center.add(dx, dy, dz)).getBlock() == Blocks.COBWEB) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
