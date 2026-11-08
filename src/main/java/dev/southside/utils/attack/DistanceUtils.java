package dev.southside.utils.attack;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;

public class DistanceUtils {
   public static double getAttackDistance(class_1657 attacker, class_1297 target) {
      class_243 attackerPos = attacker.method_33571();
      class_243 targetPos = getNearestPointBB(attackerPos, target.method_5829());
      return attackerPos.method_1022(targetPos);
   }

   public static class_243 getNearestPointBB(class_243 eye, class_238 box) {
      double originX = eye.field_1352;
      double originY = eye.field_1351;
      double originZ = eye.field_1350;
      double destMinX = box.field_1323;
      double destMinY = box.field_1322;
      double destMinZ = box.field_1321;
      double destMaxX = box.field_1320;
      double destMaxY = box.field_1325;
      double destMaxZ = box.field_1324;
      double nearestX = Math.max(destMinX, Math.min(originX, destMaxX));
      double nearestY = Math.max(destMinY, Math.min(originY, destMaxY));
      double nearestZ = Math.max(destMinZ, Math.min(originZ, destMaxZ));
      return new class_243(nearestX, nearestY, nearestZ);
   }
}
