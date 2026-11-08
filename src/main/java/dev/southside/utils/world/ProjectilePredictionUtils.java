package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class ProjectilePredictionUtils implements MinecraftAccessor {
   public static final double SNOWBALL_SPEED = 1.5;
   public static final double SNOWBALL_AIR_DRAG = 0.99;
   private static final double GROUND_FRICTION = 0.91;
   private static final int MAX_FLIGHT_TICKS = 8;
   private static final int HISTORY_SIZE = 5;
   private static final Map<UUID, ArrayDeque<class_243>> positionHistory = new HashMap<>();

   public static void recordPosition(class_1309 entity) {
      UUID id = entity.method_5667();
      ArrayDeque<class_243> history = positionHistory.computeIfAbsent(id, k -> new ArrayDeque<>());
      history.addLast(entity.method_19538());

      while (history.size() > 5) {
         history.removeFirst();
      }
   }

   public static class_243 getSmoothedVelocity(class_1309 entity) {
      ArrayDeque<class_243> history = positionHistory.get(entity.method_5667());
      if (history != null && history.size() >= 2) {
         class_243 oldest = history.peekFirst();
         class_243 newest = history.peekLast();
         int ticks = history.size() - 1;
         return new class_243(
            (newest.field_1352 - oldest.field_1352) / ticks, (newest.field_1351 - oldest.field_1351) / ticks, (newest.field_1350 - oldest.field_1350) / ticks
         );
      } else {
         return new class_243(entity.method_23317() - entity.field_6014, entity.method_23318() - entity.field_6036, entity.method_23321() - entity.field_5969);
      }
   }

   public static void clearHistory() {
      positionHistory.clear();
   }

   public static void removeEntity(UUID id) {
      positionHistory.remove(id);
   }

   public static int estimateFlightTicks(double horizontalDistance, double initialSpeed, double drag) {
      double traveled = 0.0;
      double speed = initialSpeed;

      int ticks;
      for (ticks = 0; traveled < horizontalDistance && ticks < 8; ticks++) {
         traveled += speed;
         speed *= drag;
      }

      return ticks;
   }

   public static class_243 predictTargetPosition(class_1309 target, int ticks, boolean wallCheck) {
      class_243 velocity = getSmoothedVelocity(target);
      double predX = target.method_23317();
      double predY = target.method_23318();
      double predZ = target.method_23321();
      double vx = velocity.field_1352;
      double vy = velocity.field_1351;
      double vz = velocity.field_1350;
      boolean onGround = target.method_24828();

      for (int i = 0; i < ticks; i++) {
         predX += vx;
         predZ += vz;
         if (onGround) {
            vx *= 0.91;
            vz *= 0.91;
            if (vy <= 0.0) {
               vy = 0.0;
            }
         }

         predY += vy;
         if (!onGround) {
            vy -= 0.08;
         }
      }

      class_243 predicted = new class_243(predX, predY, predZ);
      if (wallCheck && mc.field_1687 != null) {
         class_243 currentCenter = target.method_19538().method_1031(0.0, target.method_17682() / 2.0, 0.0);
         class_243 predictedCenter = predicted.method_1031(0.0, target.method_17682() / 2.0, 0.0);
         class_3965 result = mc.field_1687
            .method_17742(new class_3959(currentCenter, predictedCenter, class_3960.field_17558, class_242.field_1348, mc.field_1724));
         if (result.method_17783() == class_240.field_1332) {
            predicted = new class_243(result.method_17784().field_1352, predY, result.method_17784().field_1350);
         }
      }

      return predicted;
   }

   public static Rotation getThrowableRotation(class_243 predictedPos) {
      double eyeX = mc.field_1724.method_23317();
      double eyeY = mc.field_1724.method_23318() + mc.field_1724.method_18381(mc.field_1724.method_18376());
      double eyeZ = mc.field_1724.method_23321();
      double dx = predictedPos.field_1352 - eyeX;
      double dz = predictedPos.field_1350 - eyeZ;
      double dy = predictedPos.field_1351 + 1.0 - eyeY;
      double horizontalDist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
      float pitch = -RotationUtils.getTrajAngleSolutionLow((float)horizontalDist, (float)dy, 1.0F);
      if (Float.isNaN(pitch)) {
         pitch = (float)(-Math.atan2(dy, horizontalDist) * 180.0 / Math.PI);
      }

      return new Rotation(yaw, pitch);
   }
}
