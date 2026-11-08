package dev.southside.utils.math;

import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;

public final class AimMath {
   public static Rotation toRotation(AimMath.Result input) {
      return new Rotation(input.yaw, input.pitch);
   }

   public static AimMath.Result computeBestAimToEntity(class_746 player, class_1297 target, float tickDelta) {
      class_243 eyePos = ClientRayTraceUtil.eyePos;
      class_238 box = target.method_5829();
      List<class_243> anchors = new ArrayList<>(4);
      class_243 closest = getClosestPointOnBox(eyePos, box);
      anchors.add(closest);
      class_243 head = getEntityEye(target, tickDelta);
      anchors.add(head);
      class_243 center = getEntityCenter(target, tickDelta);
      anchors.add(center);
      class_243 legs = getEntityLegs(target, tickDelta);
      anchors.add(legs);
      class_243 bestPoint = closest;
      float minAngleCost = Float.MAX_VALUE;
      boolean foundVisible = false;
      float curYaw = player.method_5705(tickDelta);
      float curPitch = player.method_5695(tickDelta);
      double range = player.method_5739(target) + 6.0;

      for (class_243 point : anchors) {
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

   private static class_243 getClosestPointOnBox(class_243 point, class_238 box) {
      return RotationUtils.getClosestPoint(point, box);
   }

   private static class_243 getEntityEye(class_1297 e, float tickDelta) {
      class_238 box = e.method_5829();
      double cx = (box.field_1323 + box.field_1320) * 0.5;
      double cz = (box.field_1321 + box.field_1324) * 0.5;
      double eyeY = class_3532.method_16436(0.8, box.field_1322, box.field_1325);
      return new class_243(cx, eyeY, cz);
   }

   private static class_243 getEntityLegs(class_1297 e, float tickDelta) {
      class_238 box = e.method_5829();
      double cx = (box.field_1323 + box.field_1320) * 0.5;
      double cz = (box.field_1321 + box.field_1324) * 0.5;
      double legY = class_3532.method_16436(0.15, box.field_1322, box.field_1325);
      return new class_243(cx, legY, cz);
   }

   private static class_243 getEntityCenter(class_1297 e, float tickDelta) {
      class_238 box = e.method_5829();
      double cx = (box.field_1323 + box.field_1320) * 0.5;
      double cy = (box.field_1322 + box.field_1325) * 0.5;
      double cz = (box.field_1321 + box.field_1324) * 0.5;
      return new class_243(cx, cy, cz);
   }

   private static float[] calcYawPitch(class_243 from, class_243 to) {
      double dx = to.field_1352 - from.field_1352;
      double dy = to.field_1351 - from.field_1351;
      double dz = to.field_1350 - from.field_1350;
      double distXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
      yaw = class_3532.method_15393(yaw);
      return new float[]{yaw, pitch};
   }

   private static float angleDiff(float a, float b) {
      return Math.abs(class_3532.method_15393(a - b));
   }

   public static final class Result {
      public final float yaw;
      public final float pitch;
      public final class_243 aimPoint;
      public final boolean visible;
      public final float deltaYaw;
      public final float deltaPitch;

      public Result(float yaw, float pitch, class_243 aimPoint, boolean visible, float deltaYaw, float deltaPitch) {
         this.yaw = yaw;
         this.pitch = pitch;
         this.aimPoint = aimPoint;
         this.visible = visible;
         this.deltaYaw = deltaYaw;
         this.deltaPitch = deltaPitch;
      }
   }
}
