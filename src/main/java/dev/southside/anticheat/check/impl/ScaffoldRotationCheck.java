package dev.southside.anticheat.check.impl;

import dev.southside.anticheat.check.NormalCheck;
import dev.southside.anticheat.player.PlayerData;
import java.util.LinkedList;
import net.minecraft.entity.player.PlayerEntity;

public class ScaffoldRotationCheck implements NormalCheck {
   private static final double SIGNIFICANT_YAW_CHANGE = 35.0;
   private static final double ANGLE_CHANGE_SUM_THRESHOLD = 360.0;
   private static final double ANGLE_VARIANCE_THRESHOLD = 70.0;
   private static final double ANGLE_SWITCH_THRESHOLD = 20.0;
   private static final int YAW_HISTORY_SIZE = 10;
   private static final int COOLDOWN_TICKS = 40;

   @Override
   public void onUpdate(PlayerData playerData) {
      PlayerEntity player = playerData.getPlayer();
      int tick = player.age;
      float currentYaw = player.getYaw();
      LinkedList<Float> yawHistory = playerData.getScaffoldYawHistory();
      yawHistory.addLast(currentYaw);
      if (yawHistory.size() > 10) {
         yawHistory.removeFirst();
      }

      if (this.isScaffolding(playerData)) {
         if (yawHistory.size() >= 4) {
            if (tick - playerData.getScaffoldJumpLastFlagTick() >= 40 || tick - playerData.getScaffoldBatchLastFlagTick() >= 40) {
               double[] deltas = new double[yawHistory.size() - 1];

               for (int i = 0; i < deltas.length; i++) {
                  deltas[i] = normalizeAngle(yawHistory.get(i + 1) - yawHistory.get(i));
               }

               boolean recentSnap = false;

               for (int i = Math.max(0, deltas.length - 2); i < deltas.length; i++) {
                  if (Math.abs(deltas[i]) > 35.0) {
                     recentSnap = true;
                     break;
                  }
               }

               if (recentSnap) {
                  playerData.setScaffoldRotSignificantFails(playerData.getScaffoldRotSignificantFails() + 1);
                  if (playerData.getScaffoldRotSignificantFails() >= 3) {
                     playerData.addVl(3.0F, "Scaffold (Rotation Snap)");
                     playerData.setScaffoldRotSignificantFails(0);
                  }
               } else if (playerData.getScaffoldRotSignificantFails() > 0) {
                  playerData.setScaffoldRotSignificantFails(playerData.getScaffoldRotSignificantFails() - 1);
               }

               double angleSum = 0.0;

               for (double d : deltas) {
                  angleSum += Math.abs(d);
               }

               if (angleSum > 360.0) {
                  playerData.setScaffoldRotAngleSumFails(playerData.getScaffoldRotAngleSumFails() + 1);
                  if (playerData.getScaffoldRotAngleSumFails() >= 2) {
                     playerData.addVl(2.0F, "Scaffold (Rotation Sum)");
                     playerData.setScaffoldRotAngleSumFails(0);
                  }
               } else if (playerData.getScaffoldRotAngleSumFails() > 0) {
                  playerData.setScaffoldRotAngleSumFails(playerData.getScaffoldRotAngleSumFails() - 1);
               }

               double mean = angleSum / deltas.length;
               double varianceSum = 0.0;

               for (double d : deltas) {
                  double diff = Math.abs(d) - mean;
                  varianceSum += diff * diff;
               }

               double variance = varianceSum / deltas.length;
               if (variance > 70.0) {
                  playerData.setScaffoldRotVarianceFails(playerData.getScaffoldRotVarianceFails() + 1);
                  if (playerData.getScaffoldRotVarianceFails() >= 3) {
                     playerData.addVl(1.5F, "Scaffold (Rotation Variance)");
                     playerData.setScaffoldRotVarianceFails(0);
                  }
               } else if (playerData.getScaffoldRotVarianceFails() > 0) {
                  playerData.setScaffoldRotVarianceFails(playerData.getScaffoldRotVarianceFails() - 1);
               }

               int largeTurns = 0;

               for (double d : deltas) {
                  if (Math.abs(d) > 20.0) {
                     largeTurns++;
                  }
               }

               if (largeTurns > 4) {
                  playerData.setScaffoldRotSwitchFails(playerData.getScaffoldRotSwitchFails() + 1);
                  if (playerData.getScaffoldRotSwitchFails() >= 2) {
                     playerData.addVl(1.5F, "Scaffold (Rotation Switch)");
                     playerData.setScaffoldRotSwitchFails(0);
                  }
               } else if (playerData.getScaffoldRotSwitchFails() > 0) {
                  playerData.setScaffoldRotSwitchFails(playerData.getScaffoldRotSwitchFails() - 1);
               }
            }
         }
      }
   }

   private boolean isScaffolding(PlayerData playerData) {
      return playerData.getScaffoldLastPlacedBlock() != null && !playerData.getScaffoldPlaceTimes().isEmpty();
   }

   private static double normalizeAngle(double angle) {
      angle %= 360.0;
      if (angle > 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }
}
