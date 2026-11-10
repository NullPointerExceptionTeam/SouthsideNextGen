package dev.southside.utils.math;

import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;

public final class AimMath {
   public static Rotation toRotation(AimMath.Result input) {
      return new Rotation(input.yaw, input.pitch);
   }

   public static AimMath.Result computeBestAimToEntity(ClientPlayerEntity player, Entity target, float tickDelta) {
      Vec3d eyePos = ClientRayTraceUtil.eyePos;
      Box box = target.getBoundingBox();
      List<Vec3d> anchors = new ArrayList<>(4);
      Vec3d closest = getClosestPointOnBox(eyePos, box);
      anchors.add(closest);
      Vec3d head = getEntityEye(target, tickDelta);
      anchors.add(head);
      Vec3d center = getEntityCenter(target, tickDelta);
      anchors.add(center);
      Vec3d legs = getEntityLegs(target, tickDelta);
      anchors.add(legs);
      Vec3d bestPoint = closest;
      float minAngleCost = Float.MAX_VALUE;
      boolean foundVisible = false;
      float curYaw = player.getYaw(tickDelta);
      float curPitch = player.getPitch(tickDelta);
      double range = player.distanceTo(target) + 6.0;

      for (Vec3d point : anchors) {
         float[] rot = calcYawPitch(eyePos, point);
         boolean isVisible = ClientRayTraceUtil.didHitEntity(rot[0], rot[1], range, target);
         float yawDiff = angleDiff(curYaw, rot[0]);
         float pitchDiff = Math.abs(curPitch - rot[1]);
         float totalCost = yawDiff + pitchDiff;
         if (isVisible) {
            if (!foundVisible || totalCost < minAngleCost) {
               foundVisible = true;
               minAngleCost = totalCost;
               bestPoint = point;
            }
         } else if (!foundVisible && totalCost < minAngleCost) {
            minAngleCost = totalCost;
            bestPoint = point;
         }
      }

      float[] finalRot = calcYawPitch(eyePos, bestPoint);
      float dYaw = angleDiff(finalRot[0], curYaw);
      float dPitch = Math.abs(finalRot[1] - curPitch);
      return new AimMath.Result(finalRot[0], finalRot[1], bestPoint, foundVisible, dYaw, dPitch);
   }

   private static Vec3d getClosestPointOnBox(Vec3d point, Box box) {
      return RotationUtils.getClosestPoint(point, box);
   }

   private static Vec3d getEntityEye(Entity e, float tickDelta) {
      Box box = e.getBoundingBox();
      double cx = (box.minX + box.maxX) * 0.5;
      double cz = (box.minZ + box.maxZ) * 0.5;
      double eyeY = MathHelper.lerp(0.8, box.minY, box.maxY);
      return new Vec3d(cx, eyeY, cz);
   }

   private static Vec3d getEntityLegs(Entity e, float tickDelta) {
      Box box = e.getBoundingBox();
      double cx = (box.minX + box.maxX) * 0.5;
      double cz = (box.minZ + box.maxZ) * 0.5;
      double legY = MathHelper.lerp(0.15, box.minY, box.maxY);
      return new Vec3d(cx, legY, cz);
   }

   private static Vec3d getEntityCenter(Entity e, float tickDelta) {
      Box box = e.getBoundingBox();
      double cx = (box.minX + box.maxX) * 0.5;
      double cy = (box.minY + box.maxY) * 0.5;
      double cz = (box.minZ + box.maxZ) * 0.5;
      return new Vec3d(cx, cy, cz);
   }

   private static float[] calcYawPitch(Vec3d from, Vec3d to) {
      double dx = to.x - from.x;
      double dy = to.y - from.y;
      double dz = to.z - from.z;
      double distXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
      yaw = MathHelper.wrapDegrees(yaw);
      return new float[]{yaw, pitch};
   }

   private static float angleDiff(float a, float b) {
      return Math.abs(MathHelper.wrapDegrees(a - b));
   }

   public static final class Result {
      public final float yaw;
      public final float pitch;
      public final Vec3d aimPoint;
      public final boolean visible;
      public final float deltaYaw;
      public final float deltaPitch;

      public Result(float yaw, float pitch, Vec3d aimPoint, boolean visible, float deltaYaw, float deltaPitch) {
         this.yaw = yaw;
         this.pitch = pitch;
         this.aimPoint = aimPoint;
         this.visible = visible;
         this.deltaYaw = deltaYaw;
         this.deltaPitch = deltaPitch;
      }
   }
}
