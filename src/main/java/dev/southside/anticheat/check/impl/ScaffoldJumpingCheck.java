package dev.southside.anticheat.check.impl;

import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.player.PlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ScaffoldJumpingCheck implements NormalCheck {
   private static final int STAIR_BYPASS_TICKS = 50;
   private static final int COOLDOWN_TICKS = 40;
   private static final int LEGIT_DECAY_INTERVAL = 18;
   private static final double MIN_XZ_SPEED = 0.05;
   private static final double JUMP_VELOCITY_THRESHOLD = 0.2;

   @Override
   public void onUpdate(PlayerData playerData) {
      PlayerEntity player = playerData.getPlayer();
      int tick = player.age;
      if (playerData.getScaffoldLastPlacedBlock() != null) {
         if (!playerData.getScaffoldPlaceTimes().isEmpty()) {
            if (playerData.getScaffoldLastPlacedY() != -1) {
               int currentPlaceY = playerData.getScaffoldLastPlacedBlock().getY();
               if (currentPlaceY > playerData.getScaffoldLastPlacedY()) {
                  playerData.setScaffoldLastStairTick(tick);
               }
            }

            Vec3d velocity = player.getVelocity();
            double xzSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            boolean movingHorizontally = xzSpeed > 0.05;
            boolean jumping = !player.isOnGround() && velocity.y > 0.2;
            boolean notStair = tick - playerData.getScaffoldLastStairTick() > 50;
            if (movingHorizontally && jumping && notStair) {
               int fails = playerData.getScaffoldJumpFails() + 1;
               playerData.setScaffoldJumpFails(fails);
               if (fails >= 3 && tick - playerData.getScaffoldJumpLastFlagTick() > 40) {
                  playerData.addVl(4.0F, "Scaffold (Jumping)");
                  playerData.setScaffoldJumpLastFlagTick(tick);
                  playerData.setScaffoldJumpFails(0);
               }
            } else {
               int legit = playerData.getScaffoldJumpLegitCount() + 1;
               playerData.setScaffoldJumpLegitCount(legit);
               if (legit >= 18) {
                  if (playerData.getScaffoldJumpFails() > 0) {
                     playerData.setScaffoldJumpFails(playerData.getScaffoldJumpFails() - 1);
                  }

                  playerData.setScaffoldJumpLegitCount(0);
               }
            }
         }
      }
   }
}
