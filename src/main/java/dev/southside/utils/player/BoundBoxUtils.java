package dev.southside.utils.player;

import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3965;

public class BoundBoxUtils {
   public static class_243 getMinDistanceVec3dByBox(class_243 eyePosition, class_238 box) {
      return RotationUtils.getClosestPoint(eyePosition, box);
   }

   public static double getMinimumOnRange(double target, double min, double max) {
      return target <= min ? min : Math.min(target, max);
   }

   public static class_3965 calculateIntercept(class_238 targetBox, class_243 eyesPos, Rotation rotation, double range) {
      class_243 rotationVec = rotation.getRotationVec();
      class_243 hitVec = eyesPos.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
      class_243 vec3 = getIntermediateWithXValue(eyesPos, hitVec, targetBox.field_1323);
      class_243 vec31 = getIntermediateWithXValue(eyesPos, hitVec, targetBox.field_1320);
      class_243 vec32 = getIntermediateWithYValue(eyesPos, hitVec, targetBox.field_1322);
      class_243 vec33 = getIntermediateWithYValue(eyesPos, hitVec, targetBox.field_1325);
      class_243 vec34 = getIntermediateWithZValue(eyesPos, hitVec, targetBox.field_1321);
      class_243 vec35 = getIntermediateWithZValue(eyesPos, hitVec, targetBox.field_1324);
      if (!isVecInYZ(targetBox, vec3)) {
         vec3 = null;
      }

      if (!isVecInYZ(targetBox, vec31)) {
         vec31 = null;
      }

      if (!isVecInXZ(targetBox, vec32)) {
         vec32 = null;
      }

      if (!isVecInXZ(targetBox, vec33)) {
         vec33 = null;
      }

      if (!isVecInXY(targetBox, vec34)) {
         vec34 = null;
      }

      if (!isVecInXY(targetBox, vec35)) {
         vec35 = null;
      }

      class_243 vec36 = null;
      if (vec3 != null) {
         vec36 = vec3;
      }

      if (vec31 != null && (vec36 == null || eyesPos.method_1025(vec31) < eyesPos.method_1025(vec36))) {
         vec36 = vec31;
      }

      if (vec32 != null && (vec36 == null || eyesPos.method_1025(vec32) < eyesPos.method_1025(vec36))) {
         vec36 = vec32;
      }

      if (vec33 != null && (vec36 == null || eyesPos.method_1025(vec33) < eyesPos.method_1025(vec36))) {
         vec36 = vec33;
      }

      if (vec34 != null && (vec36 == null || eyesPos.method_1025(vec34) < eyesPos.method_1025(vec36))) {
         vec36 = vec34;
      }

      if (vec35 != null && (vec36 == null || eyesPos.method_1025(vec35) < eyesPos.method_1025(vec36))) {
         vec36 = vec35;
      }

      if (vec36 == null) {
         return null;
      } else {
         class_2350 enumfacing = null;
         if (vec36 == vec3) {
            enumfacing = class_2350.field_11039;
         } else if (vec36 == vec31) {
            enumfacing = class_2350.field_11034;
         } else if (vec36 == vec32) {
            enumfacing = class_2350.field_11033;
         } else if (vec36 == vec33) {
            enumfacing = class_2350.field_11036;
         } else if (vec36 == vec34) {
            enumfacing = class_2350.field_11043;
         } else {
            enumfacing = class_2350.field_11035;
         }

         return new class_3965(vec36, enumfacing, class_2338.field_10980, false);
      }
   }

   private static class_243 getIntermediateWithXValue(class_243 target, class_243 vec, double x) {
      double d0 = vec.field_1352 - target.field_1352;
      double d1 = vec.field_1351 - target.field_1351;
      double d2 = vec.field_1350 - target.field_1350;
      if (d0 * d0 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (x - target.field_1352) / d0;
         return d3 >= 0.0 && d3 <= 1.0 ? new class_243(target.field_1352 + d0 * d3, target.field_1351 + d1 * d3, target.field_1350 + d2 * d3) : null;
      }
   }

   private static class_243 getIntermediateWithYValue(class_243 target, class_243 vec, double y) {
      double d0 = vec.field_1352 - target.field_1352;
      double d1 = vec.field_1351 - target.field_1351;
      double d2 = vec.field_1350 - target.field_1350;
      if (d1 * d1 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (y - target.field_1351) / d1;
         return d3 >= 0.0 && d3 <= 1.0 ? new class_243(target.field_1352 + d0 * d3, target.field_1351 + d1 * d3, target.field_1350 + d2 * d3) : null;
      }
   }

   private static class_243 getIntermediateWithZValue(class_243 target, class_243 vec, double z) {
      double d0 = vec.field_1352 - target.field_1352;
      double d1 = vec.field_1351 - target.field_1351;
      double d2 = vec.field_1350 - target.field_1350;
      if (d2 * d2 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (z - target.field_1350) / d2;
         return d3 >= 0.0 && d3 <= 1.0 ? new class_243(target.field_1352 + d0 * d3, target.field_1351 + d1 * d3, target.field_1350 + d2 * d3) : null;
      }
   }

   public static boolean isVecInYZ(class_238 box, class_243 vec) {
      return vec == null
         ? false
         : vec.field_1351 >= box.field_1322 && vec.field_1351 <= box.field_1325 && vec.field_1350 >= box.field_1321 && vec.field_1350 <= box.field_1324;
   }

   public static boolean isVecInXZ(class_238 box, class_243 vec) {
      return vec == null
         ? false
         : vec.field_1352 >= box.field_1323 && vec.field_1352 <= box.field_1320 && vec.field_1350 >= box.field_1321 && vec.field_1350 <= box.field_1324;
   }

   public static boolean isVecInXY(class_238 box, class_243 vec) {
      return vec == null
         ? false
         : vec.field_1352 >= box.field_1323 && vec.field_1352 <= box.field_1320 && vec.field_1351 >= box.field_1322 && vec.field_1351 <= box.field_1325;
   }
}
