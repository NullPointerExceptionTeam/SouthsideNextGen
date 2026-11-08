package dev.southside.anticheat.check.impl;

import dev.southside.MinecraftAccessor;
import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.player.PlayerData;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;

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
      class_1657 player = playerData.getPlayer();
      int tick = player.field_6012;
      if (MinecraftAccessor.mc.field_1687 != null && player.method_5805()) {
         class_2338 feetPos = player.method_24515();
         class_2338 belowFeet = feetPos.method_10074();
         boolean scaffoldDetected = false;
         class_2338 placedBlock = null;

         for (class_2350 dir : new class_2350[]{
            class_2350.field_11033, class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039
         }) {
            class_2338 checkPos = dir == class_2350.field_11033 ? belowFeet : belowFeet.method_10093(dir);
            class_2680 state = MinecraftAccessor.mc.field_1687.method_8320(checkPos);
            if (state.method_26212(MinecraftAccessor.mc.field_1687, checkPos)
               && player.method_23318() > checkPos.method_10264()
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
               playerData.setScaffoldLastPlacedY(playerData.getScaffoldLastPlacedBlock().method_10264());
            }

            placeTimes.add(now);
            placeSneaking.add(player.method_5715());
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
               double expectedDelay = this.calculateExpectedDelay(moonwalk, curSneak, prevSneak, playerData.getPlayer().method_36454());
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

   private boolean isInRange(class_1657 player, class_2338 pos) {
      class_243 playerPos = player.method_19538();
      double dx = playerPos.field_1352 - (pos.method_10263() + 0.5);
      double dy = playerPos.field_1351 - (pos.method_10264() + 0.5);
      double dz = playerPos.field_1350 - (pos.method_10260() + 0.5);
      return Math.sqrt(dx * dx + dy * dy + dz * dz) <= 4.0;
   }

   private int countSolidHorizontalNeighbors(class_2338 pos) {
      int count = 0;

      for (class_2350 dir : new class_2350[]{class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039}) {
         class_2338 neighbor = pos.method_10093(dir);
         if (MinecraftAccessor.mc.field_1687.method_8320(neighbor).method_26212(MinecraftAccessor.mc.field_1687, neighbor)) {
            count++;
         }
      }

      return count;
   }
}
