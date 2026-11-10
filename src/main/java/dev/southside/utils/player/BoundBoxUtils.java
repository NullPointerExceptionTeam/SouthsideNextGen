package dev.southside.utils.player;

import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;

public class BoundBoxUtils {
   public static Vec3d getMinDistanceVec3dByBox(Vec3d eyePosition, Box box) {
      return RotationUtils.getClosestPoint(eyePosition, box);
   }

   public static double getMinimumOnRange(double target, double min, double max) {
      return target <= min ? min : Math.min(target, max);
   }

   public static BlockHitResult calculateIntercept(Box targetBox, Vec3d eyesPos, Rotation rotation, double range) {
      Vec3d rotationVec = rotation.getRotationVec();
      Vec3d hitVec = eyesPos.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
      Vec3d vec3 = getIntermediateWithXValue(eyesPos, hitVec, targetBox.minX);
      Vec3d vec31 = getIntermediateWithXValue(eyesPos, hitVec, targetBox.maxX);
      Vec3d vec32 = getIntermediateWithYValue(eyesPos, hitVec, targetBox.minY);
      Vec3d vec33 = getIntermediateWithYValue(eyesPos, hitVec, targetBox.maxY);
      Vec3d vec34 = getIntermediateWithZValue(eyesPos, hitVec, targetBox.minZ);
      Vec3d vec35 = getIntermediateWithZValue(eyesPos, hitVec, targetBox.maxZ);
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

      Vec3d vec36 = null;
      if (vec3 != null) {
         vec36 = vec3;
      }

      if (vec31 != null && (vec36 == null || eyesPos.squaredDistanceTo(vec31) < eyesPos.squaredDistanceTo(vec36))) {
         vec36 = vec31;
      }

      if (vec32 != null && (vec36 == null || eyesPos.squaredDistanceTo(vec32) < eyesPos.squaredDistanceTo(vec36))) {
         vec36 = vec32;
      }

      if (vec33 != null && (vec36 == null || eyesPos.squaredDistanceTo(vec33) < eyesPos.squaredDistanceTo(vec36))) {
         vec36 = vec33;
      }

      if (vec34 != null && (vec36 == null || eyesPos.squaredDistanceTo(vec34) < eyesPos.squaredDistanceTo(vec36))) {
         vec36 = vec34;
      }

      if (vec35 != null && (vec36 == null || eyesPos.squaredDistanceTo(vec35) < eyesPos.squaredDistanceTo(vec36))) {
         vec36 = vec35;
      }

      if (vec36 == null) {
         return null;
      } else {
         Direction enumfacing = null;
         if (vec36 == vec3) {
            enumfacing = Direction.WEST;
         } else if (vec36 == vec31) {
            enumfacing = Direction.EAST;
         } else if (vec36 == vec32) {
            enumfacing = Direction.DOWN;
         } else if (vec36 == vec33) {
            enumfacing = Direction.UP;
         } else if (vec36 == vec34) {
            enumfacing = Direction.NORTH;
         } else {
            enumfacing = Direction.SOUTH;
         }

         return new BlockHitResult(vec36, enumfacing, BlockPos.ORIGIN, false);
      }
   }

   private static Vec3d getIntermediateWithXValue(Vec3d target, Vec3d vec, double x) {
      double d0 = vec.x - target.x;
      double d1 = vec.y - target.y;
      double d2 = vec.z - target.z;
      if (d0 * d0 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (x - target.x) / d0;
         return d3 >= 0.0 && d3 <= 1.0 ? new Vec3d(target.x + d0 * d3, target.y + d1 * d3, target.z + d2 * d3) : null;
      }
   }

   private static Vec3d getIntermediateWithYValue(Vec3d target, Vec3d vec, double y) {
      double d0 = vec.x - target.x;
      double d1 = vec.y - target.y;
      double d2 = vec.z - target.z;
      if (d1 * d1 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (y - target.y) / d1;
         return d3 >= 0.0 && d3 <= 1.0 ? new Vec3d(target.x + d0 * d3, target.y + d1 * d3, target.z + d2 * d3) : null;
      }
   }

   private static Vec3d getIntermediateWithZValue(Vec3d target, Vec3d vec, double z) {
      double d0 = vec.x - target.x;
      double d1 = vec.y - target.y;
      double d2 = vec.z - target.z;
      if (d2 * d2 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (z - target.z) / d2;
         return d3 >= 0.0 && d3 <= 1.0 ? new Vec3d(target.x + d0 * d3, target.y + d1 * d3, target.z + d2 * d3) : null;
      }
   }

   public static boolean isVecInYZ(Box box, Vec3d vec) {
      return vec == null
         ? false
         : vec.y >= box.minY && vec.y <= box.maxY && vec.z >= box.minZ && vec.z <= box.maxZ;
   }

   public static boolean isVecInXZ(Box box, Vec3d vec) {
      return vec == null
         ? false
         : vec.x >= box.minX && vec.x <= box.maxX && vec.z >= box.minZ && vec.z <= box.maxZ;
   }

   public static boolean isVecInXY(Box box, Vec3d vec) {
      return vec == null
         ? false
         : vec.x >= box.minX && vec.x <= box.maxX && vec.y >= box.minY && vec.y <= box.maxY;
   }
}
