package dev.southside.utils.attack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class DistanceUtils {
   public static double getAttackDistance(PlayerEntity attacker, Entity target) {
      Vec3d attackerPos = attacker.getEyePos();
      Vec3d targetPos = getNearestPointBB(attackerPos, target.getBoundingBox());
      return attackerPos.distanceTo(targetPos);
   }

   public static Vec3d getNearestPointBB(Vec3d eye, Box box) {
      double originX = eye.x;
      double originY = eye.y;
      double originZ = eye.z;
      double destMinX = box.minX;
      double destMinY = box.minY;
      double destMinZ = box.minZ;
      double destMaxX = box.maxX;
      double destMaxY = box.maxY;
      double destMaxZ = box.maxZ;
      double nearestX = Math.max(destMinX, Math.min(originX, destMaxX));
      double nearestY = Math.max(destMinY, Math.min(originY, destMaxY));
      double nearestZ = Math.max(destMinZ, Math.min(originZ, destMaxZ));
      return new Vec3d(nearestX, nearestY, nearestZ);
   }
}
