package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class ProjectilePredictionUtils implements MinecraftAccessor {
   public static final double SNOWBALL_SPEED = 1.5;
   public static final double SNOWBALL_AIR_DRAG = 0.99;
   private static final double GROUND_FRICTION = 0.91;
   private static final int MAX_FLIGHT_TICKS = 8;
   private static final int HISTORY_SIZE = 5;
   private static final Map<UUID, ArrayDeque<Vec3d>> positionHistory = new HashMap<>();

   public static void recordPosition(LivingEntity entity) {
      UUID id = entity.getUuid();
      ArrayDeque<Vec3d> history = positionHistory.computeIfAbsent(id, k -> new ArrayDeque<>());
      history.addLast(entity.getPos());

      while (history.size() > 5) {
         history.removeFirst();
      }
   }

   public static Vec3d getSmoothedVelocity(LivingEntity entity) {
      ArrayDeque<Vec3d> history = positionHistory.get(entity.getUuid());
      if (history != null && history.size() >= 2) {
         Vec3d oldest = history.peekFirst();
         Vec3d newest = history.peekLast();
         int ticks = history.size() - 1;
         return new Vec3d(
            (newest.x - oldest.x) / ticks, (newest.y - oldest.y) / ticks, (newest.z - oldest.z) / ticks
         );
      } else {
         return new Vec3d(entity.getX() - entity.prevX, entity.getY() - entity.prevY, entity.getZ() - entity.prevZ);
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

   public static Vec3d predictTargetPosition(LivingEntity target, int ticks, boolean wallCheck) {
      Vec3d velocity = getSmoothedVelocity(target);
      double predX = target.getX();
      double predY = target.getY();
      double predZ = target.getZ();
      double vx = velocity.x;
      double vy = velocity.y;
      double vz = velocity.z;
      boolean onGround = target.isOnGround();

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

      Vec3d predicted = new Vec3d(predX, predY, predZ);
      if (wallCheck && mc.world != null) {
         Vec3d currentCenter = target.getPos().add(0.0, target.getHeight() / 2.0, 0.0);
         Vec3d predictedCenter = predicted.add(0.0, target.getHeight() / 2.0, 0.0);
         BlockHitResult result = mc.world
            .raycast(new RaycastContext(currentCenter, predictedCenter, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
         if (result.getType() == Type.BLOCK) {
            predicted = new Vec3d(result.getPos().x, predY, result.getPos().z);
         }
      }

      return predicted;
   }

   public static Rotation getThrowableRotation(Vec3d predictedPos) {
      double eyeX = mc.player.getX();
      double eyeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
      double eyeZ = mc.player.getZ();
      double dx = predictedPos.x - eyeX;
      double dz = predictedPos.z - eyeZ;
      double dy = predictedPos.y + 1.0 - eyeY;
      double horizontalDist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
      float pitch = -RotationUtils.getTrajAngleSolutionLow((float)horizontalDist, (float)dy, 1.0F);
      if (Float.isNaN(pitch)) {
         pitch = (float)(-Math.atan2(dy, horizontalDist) * 180.0 / Math.PI);
      }

      return new Rotation(yaw, pitch);
   }
}
