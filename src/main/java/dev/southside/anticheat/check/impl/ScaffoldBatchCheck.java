package dev.southside.anticheat.check.impl;

import dev.southside.MinecraftAccessor;
import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.player.PlayerData;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;

public class ScaffoldBatchCheck implements NormalCheck {
   private static final int BATCH_SIZE = 6;
   private static final double NORMAL_DELAY_MS = 238.0;
   private static final double DIAGONAL_DELAY_MS = 138.0;
   private static final double SNEAK_ADDITION_MS = 90.0;
   private static final double SNEAK_SLOW_ADDITION_MS = 110.0;
   private static final int COOLDOWN_TICKS = 60;
   private static final double SCAFFOLD_DETECT_RANGE = 4.0;

   @Override
   public void onUpdate(PlayerData playerData) {
      PlayerEntity player = playerData.getPlayer();
      int tick = player.age;
      if (MinecraftAccessor.mc.world != null && player.isAlive()) {
         BlockPos feetPos = player.getBlockPos();
         BlockPos belowFeet = feetPos.down();
         boolean scaffoldDetected = false;
         BlockPos placedBlock = null;

         for (Direction dir : new Direction[]{
            Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
         }) {
            BlockPos checkPos = dir == Direction.DOWN ? belowFeet : belowFeet.offset(dir);
            BlockState state = MinecraftAccessor.mc.world.getBlockState(checkPos);
            if (state.isSolidBlock(MinecraftAccessor.mc.world, checkPos)
               && player.getY() > checkPos.getY()
               && this.isInRange(player, checkPos)) {
               int solidNeighbors = this.countSolidHorizontalNeighbors(checkPos);
               if (solidNeighbors == 1 && (playerData.getScaffoldLastPlacedBlock() == null || !playerData.getScaffoldLastPlacedBlock().equals(checkPos))) {
                  scaffoldDetected = true;
                  placedBlock = checkPos;
                  break;
               }
            }
         }

         if (scaffoldDetected) {
            long now = System.currentTimeMillis();
            List<Long> placeTimes = playerData.getScaffoldPlaceTimes();
            List<Boolean> placeSneaking = playerData.getScaffoldPlaceSneaking();
            if (playerData.getScaffoldLastPlacedBlock() != null) {
               playerData.setScaffoldLastPlacedY(playerData.getScaffoldLastPlacedBlock().getY());
            }

            placeTimes.add(now);
            placeSneaking.add(player.isSneaking());
            playerData.setScaffoldLastPlacedBlock(placedBlock);
            if (placeTimes.size() >= 6) {
               this.processBatch(playerData, tick);
               long lastTime = placeTimes.get(placeTimes.size() - 1);
               boolean lastSneak = placeSneaking.get(placeSneaking.size() - 1);
               placeTimes.clear();
               placeSneaking.clear();
               placeTimes.add(lastTime);
               placeSneaking.add(lastSneak);
            }
         }

         if (!playerData.getScaffoldPlaceTimes().isEmpty()) {
            long lastPlace = playerData.getScaffoldPlaceTimes().get(playerData.getScaffoldPlaceTimes().size() - 1);
            if (System.currentTimeMillis() - lastPlace > 3000L) {
               playerData.getScaffoldPlaceTimes().clear();
               playerData.getScaffoldPlaceSneaking().clear();
               playerData.setScaffoldLastPlacedBlock(null);
               playerData.setScaffoldLastPlacedY(-1);
            }
         }
      }
   }

   private void processBatch(PlayerData playerData, int tick) {
      if (tick - playerData.getScaffoldBatchLastFlagTick() >= 60) {
         List<Long> times = playerData.getScaffoldPlaceTimes();
         List<Boolean> sneaking = playerData.getScaffoldPlaceSneaking();
         if (times.size() >= 2) {
            long nonSneakCount = sneaking.stream().filter(s -> !s).count();
            boolean moonwalk = nonSneakCount >= sneaking.size() / 2;
            double actualDelaySum = 0.0;
            double expectedDelaySum = 0.0;
            int count = 0;

            for (int i = 1; i < times.size(); i++) {
               long actualDelay = times.get(i) - times.get(i - 1);
               boolean curSneak = sneaking.get(i);
               boolean prevSneak = sneaking.get(i - 1);
               double expectedDelay = this.calculateExpectedDelay(moonwalk, curSneak, prevSneak, playerData.getPlayer().getYaw());
               actualDelaySum += actualDelay;
               expectedDelaySum += expectedDelay;
               count++;
            }

            if (count != 0) {
               double actualAvg = actualDelaySum / count;
               double expectedAvg = expectedDelaySum / count;
               if (actualAvg < expectedAvg) {
                  double diff = expectedAvg - actualAvg;
                  float vl = Math.min(5.0F, (float)(1.1 * diff / 50.0 + 1.0));
                  playerData.addVl(vl, "Scaffold (Speed: " + String.format("%.0f", actualAvg) + "ms<" + String.format("%.0f", expectedAvg) + "ms)");
                  playerData.setScaffoldBatchLastFlagTick(tick);
               }
            }
         }
      }
   }

   private double calculateExpectedDelay(boolean moonwalk, boolean curSneak, boolean prevSneak, float yaw) {
      if (!moonwalk && curSneak && prevSneak) {
         double diagonalFactor = 110.0 * Math.abs(Math.cos(2.0 * Math.toRadians(yaw)));
         return 328.0 + diagonalFactor;
      } else {
         return 238.0;
      }
   }

   private boolean isInRange(PlayerEntity player, BlockPos pos) {
      Vec3d playerPos = player.getPos();
      double dx = playerPos.x - (pos.getX() + 0.5);
      double dy = playerPos.y - (pos.getY() + 0.5);
      double dz = playerPos.z - (pos.getZ() + 0.5);
      return Math.sqrt(dx * dx + dy * dy + dz * dz) <= 4.0;
   }

   private int countSolidHorizontalNeighbors(BlockPos pos) {
      int count = 0;

      for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
         BlockPos neighbor = pos.offset(dir);
         if (MinecraftAccessor.mc.world.getBlockState(neighbor).isSolidBlock(MinecraftAccessor.mc.world, neighbor)) {
            count++;
         }
      }

      return count;
   }
}
