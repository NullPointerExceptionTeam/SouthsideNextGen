package dev.southside.utils.raytrace;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import dev.southside.utils.vector.Vector2f;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1301;
import net.minecraft.class_1657;
import net.minecraft.class_1675;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class RayCastUtils implements MinecraftAccessor {
   public static class_3965 rayTraceCollidingBlocks(class_243 start, class_243 end) {
      class_3965 result = mc.field_1687.method_17742(new class_3959(start, end, class_3960.field_17558, class_242.field_1347, mc.field_1724));
      return result != null && result.method_17783() == class_240.field_1332 ? result : null;
   }

   public static class_239 rayCast(double range, float partialTicks, boolean hitFluids, Vector2f rotations) {
      class_239 objectMouseOver = null;
      class_1297 entity = mc.method_1560();
      if (entity != null && mc.field_1687 != null) {
         objectMouseOver = pick(range, partialTicks, hitFluids, rotations.getX(), rotations.getY());
      }

      return objectMouseOver;
   }

   public static class_239 rayCast(Rotation rotation, double range) {
      return rayCast(rotation, range, 0.0F);
   }

   public static class_239 rayCast(Rotation rotation, double range, float expand) {
      return rayCast(rotation, range, expand, mc.field_1724);
   }

   public static class_239 rayCast(Rotation rotation, double range, float expand, class_1297 entity) {
      if (entity != null && mc.field_1687 != null) {
         float partialTicks = mc.method_61966().method_60637(false);
         class_243 eyePosition = entity.method_5836(partialTicks);
         class_243 lookVector = RotationUtils.getVFR(rotation);
         class_243 targetVec = eyePosition.method_1031(lookVector.field_1352 * range, lookVector.field_1351 * range, lookVector.field_1350 * range);
         double expandedRange = range + expand;
         class_238 searchBox = new class_238(
            eyePosition.field_1352 - expandedRange,
            eyePosition.field_1351 - expandedRange,
            eyePosition.field_1350 - expandedRange,
            eyePosition.field_1352 + expandedRange,
            eyePosition.field_1351 + expandedRange,
            eyePosition.field_1350 + expandedRange
         );
         List<class_1297> entities = mc.field_1687
            .method_8390(class_1297.class, searchBox, ex -> ex != entity && class_1301.field_6155.test(ex) && ex.method_5863());
         class_1297 pointedEntity = null;
         class_243 hitVec = null;
         double closestDistance = range * range;

         for (class_1297 e : entities) {
            class_238 entityBox = e.method_5829().method_1014(expand);
            Optional<class_243> intercept = entityBox.method_992(eyePosition, targetVec);
            if (intercept.isPresent()) {
               class_243 interceptPoint = intercept.get();
               double distSq = eyePosition.method_1025(interceptPoint);
               if (distSq < closestDistance) {
                  closestDistance = distSq;
                  pointedEntity = e;
                  hitVec = interceptPoint;
               }
            }
         }

         return (class_239)(pointedEntity != null
            ? new class_3966(pointedEntity, hitVec)
            : mc.field_1687.method_17742(new class_3959(eyePosition, targetVec, class_3960.field_17559, class_242.field_1348, entity)));
      } else {
         return null;
      }
   }

   public static class_243 calculateViewVector(float pXRot, float pYRot) {
      float f = pXRot * (float) (Math.PI / 180.0);
      float f1 = -pYRot * (float) (Math.PI / 180.0);
      float f2 = class_3532.method_15362(f1);
      float f3 = class_3532.method_15374(f1);
      float f4 = class_3532.method_15362(f);
      float f5 = class_3532.method_15374(f);
      return new class_243(f3 * f4, -f5, f2 * f4);
   }

   public static class_239 pick(double pHitDistance, float pPartialTicks, boolean pHitFluids, float pYRot, float pXRot) {
      class_243 vec3 = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23320(), mc.field_1724.method_23321());
      class_243 vec31 = calculateViewVector(pXRot, pYRot);
      class_243 vec32 = vec3.method_1031(vec31.field_1352 * pHitDistance, vec31.field_1351 * pHitDistance, vec31.field_1350 * pHitDistance);
      return mc.field_1687
         .method_17742(new class_3959(vec3, vec32, class_3960.field_17559, pHitFluids ? class_242.field_1347 : class_242.field_1348, mc.field_1724));
   }

   public static class_239 rayCast(double blockReachDistance, float yaw, float pitch) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 start = mc.field_1724.method_5836(1.0F);
         class_243 lookVec = RotationUtils.getVectorForRotation(new Rotation(yaw, pitch));
         class_243 end = start.method_1031(
            lookVec.field_1352 * blockReachDistance, lookVec.field_1351 * blockReachDistance, lookVec.field_1350 * blockReachDistance
         );
         return mc.field_1687.method_17742(new class_3959(start, end, class_3960.field_17559, class_242.field_1348, mc.field_1724));
      } else {
         return null;
      }
   }

   public static class_239 rayCast(Rotation rotation, double range, float expand, class_1297 entity, boolean throughWall, float predict, float predictPlayer) {
      if (entity != null && mc.field_1687 != null) {
         class_239 objectMouseOver = rayTraceCustom(entity, range, rotation.getYaw(), rotation.getPitch(), predictPlayer);
         double d1 = range;
         class_243 vec3 = entity.method_5836(predictPlayer);
         if (objectMouseOver != null && objectMouseOver.method_17783() == class_240.field_1332 && !throughWall) {
            d1 = objectMouseOver.method_17784().method_1022(vec3);
         }

         class_243 vec31 = RotationUtils.getRotationVec(rotation.getPitch(), rotation.getYaw());
         class_243 vec32 = vec3.method_1031(vec31.field_1352 * range, vec31.field_1351 * range, vec31.field_1350 * range);
         class_1297 pointedEntity = null;
         class_243 vec33 = null;
         float f = 1.0F;
         List<class_1297> list = mc.field_1687
            .method_8333(
               entity,
               entity.method_5829().method_1012(vec31.field_1352 * range, vec31.field_1351 * range, vec31.field_1350 * range).method_1014(1.0),
               class_1301.field_6156.and(class_1297::method_5863)
            );
         double d2 = d1;

         for (class_1297 entity1 : list) {
            if (!entity1.method_5667().equals(mc.field_1724.method_5667())) {
               float f1 = entity1.method_5871() + expand;
               class_238 box = entity1.method_5829().method_1014(f1);
               if (predict != 0.0F) {
                  box = box.method_989(
                     (entity1.method_23317() - entity1.field_6014) * predict,
                     (entity1.method_23318() - entity1.field_6036) * predict,
                     (entity1.method_23321() - entity1.field_5969) * predict
                  );
               }

               Optional<class_243> optionalHit = box.method_992(vec3, vec32);
               class_239 movingobjectposition = null;
               if (optionalHit.isPresent()) {
                  class_243 hitPos = optionalHit.get();
                  movingobjectposition = new class_3965(
                     hitPos, class_2350.method_10142(vec31.field_1352, vec31.field_1351, vec31.field_1350), class_2338.method_49638(hitPos), false
                  );
               }

               if (box.method_1006(vec3)) {
                  if (d2 >= 0.0) {
                     pointedEntity = entity1;
                     vec33 = movingobjectposition == null ? vec3 : movingobjectposition.method_17784();
                     d2 = 0.0;
                  }
               } else if (movingobjectposition != null) {
                  double d3 = vec3.method_1022(movingobjectposition.method_17784());
                  if (d3 < d2 || d2 == 0.0) {
                     pointedEntity = entity1;
                     vec33 = movingobjectposition.method_17784();
                     d2 = d3;
                  }
               }
            }
         }

         if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new class_3966(pointedEntity, vec33);
         }

         return objectMouseOver;
      } else {
         return null;
      }
   }

   private static class_239 rayTraceCustom(class_1297 entity, double blockReachDistance, float yaw, float pitch, float partialTicks) {
      class_243 vec3 = entity.method_5836(partialTicks);
      class_243 vec31 = RotationUtils.getRotationVec(pitch, yaw);
      class_243 vec32 = vec3.method_1031(vec31.field_1352 * blockReachDistance, vec31.field_1351 * blockReachDistance, vec31.field_1350 * blockReachDistance);
      return entity.method_37908().method_17742(new class_3959(vec3, vec32, class_3960.field_17559, class_242.field_1348, entity));
   }

   public static class_3966 raytraceEntity(double range, Rotation rotation) {
      class_1297 entity = mc.field_1719;
      if (entity == null) {
         return null;
      } else {
         class_243 cameraVec = entity.method_33571();
         class_243 rotationVec = rotation.getRotationVec();
         class_243 vec3d3 = cameraVec.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
         class_238 box = entity.method_5829().method_18804(rotationVec.method_1021(range)).method_1009(1.0, 1.0, 1.0);
         return class_1675.method_18075(entity, cameraVec, vec3d3, box, e -> !e.method_7325() && e.method_5863(), range * range);
      }
   }

   public static class_3965 raytraceBlock(double range, Rotation rotation, class_2338 pos, class_2680 state) {
      class_1297 entity = mc.field_1719;
      if (entity == null) {
         return null;
      } else {
         class_243 start = entity.method_33571();
         class_243 rotationVec = rotation.getRotationVec();
         class_243 end = start.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
         return mc.field_1687.method_17745(start, end, pos, state.method_26172(mc.field_1687, pos, class_3726.method_16195(mc.field_1724)), state);
      }
   }

   public static class_3965 raycast(double range, Rotation rotation, boolean includeFluids, float tickDelta) {
      class_1297 entity = mc.field_1719;
      if (entity == null) {
         return null;
      } else {
         class_243 start = mc.field_1724.method_5836(tickDelta);
         class_243 rotationVec = rotation.getRotationVec();
         class_243 end = start.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
         return mc.field_1687
            .method_17742(new class_3959(start, end, class_3960.field_17559, includeFluids ? class_242.field_1347 : class_242.field_1348, entity));
      }
   }

   public static boolean canSeePointFrom(class_243 eyes, class_243 vec3) {
      return mc.field_1687.method_17742(new class_3959(eyes, vec3, class_3960.field_17559, class_242.field_1348, mc.field_1724)).method_17783()
         == class_240.field_1333;
   }

   public static boolean facingEnemy(class_1297 fromEntity, class_1297 toEntity, Rotation rotation, double range, double wallsRange) {
      class_243 cameraVec = fromEntity.method_33571();
      class_243 rotationVec = rotation.getRotationVec();
      double rangeSquared = range * range;
      double wallsRangeSquared = wallsRange * wallsRange;
      class_243 vec3d3 = cameraVec.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
      class_238 box = fromEntity.method_5829().method_18804(rotationVec.method_1021(range)).method_1009(1.0, 1.0, 1.0);
      class_239 entityHitResult = class_1675.method_18075(
         fromEntity, cameraVec, vec3d3, box, e -> !e.method_7325() && e.method_5863() && e == toEntity, rangeSquared
      );
      if (entityHitResult == null) {
         return false;
      } else {
         double distance = cameraVec.method_1025(entityHitResult.method_17784());
         return distance <= rangeSquared && canSeePointFrom(cameraVec, entityHitResult.method_17784()) || distance <= wallsRangeSquared;
      }
   }

   public static boolean facingBlock(class_243 eyes, class_243 vec3, class_2338 blockPos, class_2350 expectedSide, Double expectedMaxRange) {
      class_3965 searchedPos = mc.field_1687.method_17742(new class_3959(eyes, vec3, class_3960.field_17559, class_242.field_1348, mc.field_1724));
      if (searchedPos.method_17783() == class_240.field_1332 && (expectedSide == null || searchedPos.method_17780() == expectedSide)) {
         return expectedMaxRange != null && searchedPos.method_17784().method_1025(eyes) > expectedMaxRange * expectedMaxRange
            ? false
            : searchedPos.method_17777().equals(blockPos);
      } else {
         return false;
      }
   }

   public static boolean overBlock(class_1937 world, class_1657 player, Rotation rotation, class_2350 direction, class_2338 targetPos, boolean strict) {
      class_243 start = player.method_5836(1.0F);
      class_243 directionVec = RotationUtils.getDirectionVector(rotation.getYaw(), rotation.getPitch());
      class_243 end = start.method_1019(directionVec.method_1021(4.5));
      class_3965 hitResult = world.method_17742(new class_3959(start, end, class_3960.field_17559, class_242.field_1348, player));
      if (hitResult == null || hitResult.method_17783() != class_240.field_1332) {
         return false;
      } else {
         return !hitResult.method_17777().equals(targetPos) ? false : !strict || hitResult.method_17780().equals(direction);
      }
   }

   @FunctionalInterface
   public interface EntityFilter {
      boolean test(class_1297 var1);
   }
}
