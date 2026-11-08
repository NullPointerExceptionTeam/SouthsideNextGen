package dev.southside.anticheat.check.impl;

import dev.southside.MinecraftAccessor;
import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.player.PlayerData;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2338;

public class FastWebCheck implements NormalCheck {
   private static final double SPEED_THRESHOLD_PER_TICK = 0.04;
   private static final int FLAG_COOLDOWN_TICKS = 20;

   @Override
   public void onUpdate(PlayerData playerData) {
      class_1657 player = playerData.getPlayer();
      int tick = player.field_6012;
      if (MinecraftAccessor.mc.field_1687 != null && player.method_5805()) {
         boolean nearCobweb = this.isNearCobweb(player);
         if (nearCobweb) {
            double vx = player.method_18798().field_1352;
            double vz = player.method_18798().field_1350;
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

   private boolean isNearCobweb(class_1657 player) {
      class_2338 center = player.method_24515();

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (MinecraftAccessor.mc.field_1687.method_8320(center.method_10069(dx, dy, dz)).method_26204() == class_2246.field_10343) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
