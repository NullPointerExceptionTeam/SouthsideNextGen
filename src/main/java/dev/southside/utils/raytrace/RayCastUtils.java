package dev.southside.utils.raytrace;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import dev.southside.utils.vector.Vector2f;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class RayCastUtils implements MinecraftAccessor {
   public static BlockHitResult rayTraceCollidingBlocks(Vec3d start, Vec3d end) {
      BlockHitResult result = mc.world.raycast(new RaycastContext(start, end, ShapeType.COLLIDER, FluidHandling.ANY, mc.player));
      return result != null && result.getType() == Type.BLOCK ? result : null;
   }

   public static HitResult rayCast(double range, float partialTicks, boolean hitFluids, Vector2f rotations) {
      HitResult objectMouseOver = null;
      Entity entity = mc.getCameraEntity();
      if (entity != null && mc.world != null) {
         objectMouseOver = pick(range, partialTicks, hitFluids, rotations.getX(), rotations.getY());
      }

      return objectMouseOver;
   }

   public static HitResult rayCast(Rotation rotation, double range) {
      return rayCast(rotation, range, 0.0F);
   }

   public static HitResult rayCast(Rotation rotation, double range, float expand) {
      return rayCast(rotation, range, expand, mc.player);
   }

   public static HitResult rayCast(Rotation rotation, double range, float expand, Entity entity) {
      if (entity != null && mc.world != null) {
         float partialTicks = mc.getRenderTickCounter().getTickDelta(false);
         Vec3d eyePosition = entity.getCameraPosVec(partialTicks);
         Vec3d lookVector = RotationUtils.getVFR(rotation);
         Vec3d targetVec = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range);
         double expandedRange = range + expand;
         Box searchBox = new Box(
            eyePosition.x - expandedRange,
            eyePosition.y - expandedRange,
            eyePosition.z - expandedRange,
            eyePosition.x + expandedRange,
            eyePosition.y + expandedRange,
            eyePosition.z + expandedRange
         );
         List<Entity> entities = mc.world
            .getEntitiesByClass(Entity.class, searchBox, ex -> ex != entity && EntityPredicates.EXCEPT_SPECTATOR.test(ex) && ex.canHit());
         Entity pointedEntity = null;
         Vec3d hitVec = null;
         double closestDistance = range * range;

         for (Entity e : entities) {
            Box entityBox = e.getBoundingBox().expand(expand);
            Optional<Vec3d> intercept = entityBox.raycast(eyePosition, targetVec);
            if (intercept.isPresent()) {
               Vec3d interceptPoint = intercept.get();
               double distSq = eyePosition.squaredDistanceTo(interceptPoint);
               if (distSq < closestDistance) {
                  closestDistance = distSq;
                  pointedEntity = e;
                  hitVec = interceptPoint;
               }
            }
         }

         return (HitResult)(pointedEntity != null
            ? new EntityHitResult(pointedEntity, hitVec)
            : mc.world.raycast(new RaycastContext(eyePosition, targetVec, ShapeType.OUTLINE, FluidHandling.NONE, entity)));
      } else {
         return null;
      }
   }

   public static Vec3d calculateViewVector(float pXRot, float pYRot) {
      float f = pXRot * (float) (Math.PI / 180.0);
      float f1 = -pYRot * (float) (Math.PI / 180.0);
      float f2 = MathHelper.cos(f1);
      float f3 = MathHelper.sin(f1);
      float f4 = MathHelper.cos(f);
      float f5 = MathHelper.sin(f);
      return new Vec3d(f3 * f4, -f5, f2 * f4);
   }

   public static HitResult pick(double pHitDistance, float pPartialTicks, boolean pHitFluids, float pYRot, float pXRot) {
      Vec3d vec3 = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
      Vec3d vec31 = calculateViewVector(pXRot, pYRot);
      Vec3d vec32 = vec3.add(vec31.x * pHitDistance, vec31.y * pHitDistance, vec31.z * pHitDistance);
      return mc.world
         .raycast(new RaycastContext(vec3, vec32, ShapeType.OUTLINE, pHitFluids ? FluidHandling.ANY : FluidHandling.NONE, mc.player));
   }

   public static HitResult rayCast(double blockReachDistance, float yaw, float pitch) {
      if (mc.player != null && mc.world != null) {
         Vec3d start = mc.player.getCameraPosVec(1.0F);
         Vec3d lookVec = RotationUtils.getVectorForRotation(new Rotation(yaw, pitch));
         Vec3d end = start.add(
            lookVec.x * blockReachDistance, lookVec.y * blockReachDistance, lookVec.z * blockReachDistance
         );
         return mc.world.raycast(new RaycastContext(start, end, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
      } else {
         return null;
      }
   }

   public static HitResult rayCast(Rotation rotation, double range, float expand, Entity entity, boolean throughWall, float predict, float predictPlayer) {
      if (entity != null && mc.world != null) {
         HitResult objectMouseOver = rayTraceCustom(entity, range, rotation.getYaw(), rotation.getPitch(), predictPlayer);
         double d1 = range;
         Vec3d vec3 = entity.getCameraPosVec(predictPlayer);
         if (objectMouseOver != null && objectMouseOver.getType() == Type.BLOCK && !throughWall) {
            d1 = objectMouseOver.getPos().distanceTo(vec3);
         }

         Vec3d vec31 = RotationUtils.getRotationVec(rotation.getPitch(), rotation.getYaw());
         Vec3d vec32 = vec3.add(vec31.x * range, vec31.y * range, vec31.z * range);
         Entity pointedEntity = null;
         Vec3d vec33 = null;
         float f = 1.0F;
         List<Entity> list = mc.world
            .getOtherEntities(
               entity,
               entity.getBoundingBox().stretch(vec31.x * range, vec31.y * range, vec31.z * range).expand(1.0),
               EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(Entity::canHit)
            );
         double d2 = d1;

         for (Entity entity1 : list) {
            if (!entity1.getUuid().equals(mc.player.getUuid())) {
               float f1 = entity1.getTargetingMargin() + expand;
               Box box = entity1.getBoundingBox().expand(f1);
               if (predict != 0.0F) {
                  box = box.offset(
                     (entity1.getX() - entity1.prevX) * predict,
                     (entity1.getY() - entity1.prevY) * predict,
                     (entity1.getZ() - entity1.prevZ) * predict
                  );
               }

               Optional<Vec3d> optionalHit = box.raycast(vec3, vec32);
               HitResult movingobjectposition = null;
               if (optionalHit.isPresent()) {
                  Vec3d hitPos = optionalHit.get();
                  movingobjectposition = new BlockHitResult(
                     hitPos, Direction.getFacing(vec31.x, vec31.y, vec31.z), BlockPos.ofFloored(hitPos), false
                  );
               }

               if (box.contains(vec3)) {
                  if (d2 >= 0.0) {
                     pointedEntity = entity1;
                     vec33 = movingobjectposition == null ? vec3 : movingobjectposition.getPos();
                     d2 = 0.0;
                  }
               } else if (movingobjectposition != null) {
                  double d3 = vec3.distanceTo(movingobjectposition.getPos());
                  if (d3 < d2 || d2 == 0.0) {
                     pointedEntity = entity1;
                     vec33 = movingobjectposition.getPos();
                     d2 = d3;
                  }
               }
            }
         }

         if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new EntityHitResult(pointedEntity, vec33);
         }

         return objectMouseOver;
      } else {
         return null;
      }
   }

   private static HitResult rayTraceCustom(Entity entity, double blockReachDistance, float yaw, float pitch, float partialTicks) {
      Vec3d vec3 = entity.getCameraPosVec(partialTicks);
      Vec3d vec31 = RotationUtils.getRotationVec(pitch, yaw);
      Vec3d vec32 = vec3.add(vec31.x * blockReachDistance, vec31.y * blockReachDistance, vec31.z * blockReachDistance);
      return entity.getWorld().raycast(new RaycastContext(vec3, vec32, ShapeType.OUTLINE, FluidHandling.NONE, entity));
   }

   public static EntityHitResult raytraceEntity(double range, Rotation rotation) {
      Entity entity = mc.cameraEntity;
      if (entity == null) {
         return null;
      } else {
         Vec3d cameraVec = entity.getEyePos();
         Vec3d rotationVec = rotation.getRotationVec();
         Vec3d vec3d3 = cameraVec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
         Box box = entity.getBoundingBox().stretch(rotationVec.multiply(range)).expand(1.0, 1.0, 1.0);
         return ProjectileUtil.raycast(entity, cameraVec, vec3d3, box, e -> !e.isSpectator() && e.canHit(), range * range);
      }
   }

   public static BlockHitResult raytraceBlock(double range, Rotation rotation, BlockPos pos, BlockState state) {
      Entity entity = mc.cameraEntity;
      if (entity == null) {
         return null;
      } else {
         Vec3d start = entity.getEyePos();
         Vec3d rotationVec = rotation.getRotationVec();
         Vec3d end = start.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
         return mc.world.raycastBlock(start, end, pos, state.getOutlineShape(mc.world, pos, ShapeContext.of(mc.player)), state);
      }
   }

   public static BlockHitResult raycast(double range, Rotation rotation, boolean includeFluids, float tickDelta) {
      Entity entity = mc.cameraEntity;
      if (entity == null) {
         return null;
      } else {
         Vec3d start = mc.player.getCameraPosVec(tickDelta);
         Vec3d rotationVec = rotation.getRotationVec();
         Vec3d end = start.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
         return mc.world
            .raycast(new RaycastContext(start, end, ShapeType.OUTLINE, includeFluids ? FluidHandling.ANY : FluidHandling.NONE, entity));
      }
   }

   public static boolean canSeePointFrom(Vec3d eyes, Vec3d vec3) {
      return mc.world.raycast(new RaycastContext(eyes, vec3, ShapeType.OUTLINE, FluidHandling.NONE, mc.player)).getType()
         == Type.MISS;
   }

   public static boolean facingEnemy(Entity fromEntity, Entity toEntity, Rotation rotation, double range, double wallsRange) {
      Vec3d cameraVec = fromEntity.getEyePos();
      Vec3d rotationVec = rotation.getRotationVec();
      double rangeSquared = range * range;
      double wallsRangeSquared = wallsRange * wallsRange;
      Vec3d vec3d3 = cameraVec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
      Box box = fromEntity.getBoundingBox().stretch(rotationVec.multiply(range)).expand(1.0, 1.0, 1.0);
      HitResult entityHitResult = ProjectileUtil.raycast(
         fromEntity, cameraVec, vec3d3, box, e -> !e.isSpectator() && e.canHit() && e == toEntity, rangeSquared
      );
      if (entityHitResult == null) {
         return false;
      } else {
         double distance = cameraVec.squaredDistanceTo(entityHitResult.getPos());
         return distance <= rangeSquared && canSeePointFrom(cameraVec, entityHitResult.getPos()) || distance <= wallsRangeSquared;
      }
   }

   public static boolean facingBlock(Vec3d eyes, Vec3d vec3, BlockPos blockPos, Direction expectedSide, Double expectedMaxRange) {
      BlockHitResult searchedPos = mc.world.raycast(new RaycastContext(eyes, vec3, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
      if (searchedPos.getType() == Type.BLOCK && (expectedSide == null || searchedPos.getSide() == expectedSide)) {
         return expectedMaxRange != null && searchedPos.getPos().squaredDistanceTo(eyes) > expectedMaxRange * expectedMaxRange
            ? false
            : searchedPos.getBlockPos().equals(blockPos);
      } else {
         return false;
      }
   }

   public static boolean overBlock(World world, PlayerEntity player, Rotation rotation, Direction direction, BlockPos targetPos, boolean strict) {
      Vec3d start = player.getCameraPosVec(1.0F);
      Vec3d directionVec = RotationUtils.getDirectionVector(rotation.getYaw(), rotation.getPitch());
      Vec3d end = start.add(directionVec.multiply(4.5));
      BlockHitResult hitResult = world.raycast(new RaycastContext(start, end, ShapeType.OUTLINE, FluidHandling.NONE, player));
      if (hitResult == null || hitResult.getType() != Type.BLOCK) {
         return false;
      } else {
         return !hitResult.getBlockPos().equals(targetPos) ? false : !strict || hitResult.getSide().equals(direction);
      }
   }

   @FunctionalInterface
   public interface EntityFilter {
      boolean test(Entity var1);
   }
}
