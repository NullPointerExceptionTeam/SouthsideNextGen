package dev.southside.utils.raytrace;

import dev.southside.utils.types.Rotation;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.ShortPlantBlock;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ClientRayTraceUtil {
   private static final double EPSILON = 1.0E-7;
   public static Vec3d eyePos = null;
   private static MinecraftClient mc = MinecraftClient.getInstance();

   public static boolean didHitBlockFace(Rotation rotation, BlockPos targetPos, Direction expectedFace, boolean strict) {
      return didHitBlockFace(mc.player, rotation.yaw, rotation.pitch, targetPos, expectedFace, strict, ClientRayTraceUtil::isIgnoredBlock);
   }

   public static boolean didHitBlockFace(PlayerEntity player, float yaw, float pitch, BlockPos targetPos, Direction expectedFace, boolean strict) {
      return didHitBlockFace(player, yaw, pitch, targetPos, expectedFace, strict, ClientRayTraceUtil::isIgnoredBlock);
   }

   public static boolean didHitBlockFace(
      Rotation rotation, BlockPos targetPos, Direction expectedFace, boolean strict, Predicate<BlockState> ignorePredicate
   ) {
      return didHitBlockFace(mc.player, rotation.yaw, rotation.pitch, targetPos, expectedFace, strict, ignorePredicate);
   }

   public static boolean didHitBlockFace(
      PlayerEntity player, float yaw, float pitch, BlockPos targetPos, Direction expectedFace, boolean strict, Predicate<BlockState> ignorePredicate
   ) {
      if (player != null && expectedFace != null) {
         BlockHitResult result = getFacedBlock(yaw, pitch, ignorePredicate);
         return result != null
            && targetPos.getX() == result.getBlockPos().getX()
            && targetPos.getY() == result.getBlockPos().getY()
            && targetPos.getZ() == result.getBlockPos().getZ()
            && (expectedFace == result.getSide() || !strict);
      } else {
         return false;
      }
   }

   public static void updateEyePos() {
      if (mc.player != null) {
         eyePos = mc.player.getEyePos();
      }
   }

   @Nullable
   private static Direction getHitFace(Vec3d hitPos, Box box) {
      if (Math.abs(hitPos.x - box.minX) < 1.0E-7) {
         return Direction.WEST;
      } else if (Math.abs(hitPos.x - box.maxX) < 1.0E-7) {
         return Direction.EAST;
      } else if (Math.abs(hitPos.y - box.minY) < 1.0E-7) {
         return Direction.DOWN;
      } else if (Math.abs(hitPos.y - box.maxY) < 1.0E-7) {
         return Direction.UP;
      } else if (Math.abs(hitPos.z - box.minZ) < 1.0E-7) {
         return Direction.NORTH;
      } else {
         return Math.abs(hitPos.z - box.maxZ) < 1.0E-7 ? Direction.SOUTH : null;
      }
   }

   public static BlockHitResult getFacedBlock(float yaw, float pitch) {
      return getFacedBlock(yaw, pitch, ClientRayTraceUtil::isIgnoredBlock);
   }

   public static BlockHitResult getFacedBlock(float yaw, float pitch, Predicate<BlockState> ignorePredicate) {
      MinecraftClient client = MinecraftClient.getInstance();
      double reachDistance = client.player.getBlockInteractionRange();
      Vec3d startPos = eyePos;
      Vec3d direction = Vec3d.fromPolar(pitch, yaw);
      Vec3d endPos = startPos.add(direction.multiply(reachDistance));
      if (direction.x == 0.0) {
         direction = new Vec3d(1.0E-7, direction.y, direction.z);
      }

      if (direction.y == 0.0) {
         direction = new Vec3d(direction.x, 1.0E-7, direction.z);
      }

      if (direction.z == 0.0) {
         direction = new Vec3d(direction.x, direction.y, 1.0E-7);
      }

      BlockPos currentPos = BlockPos.ofFloored(startPos);
      int stepX = (int)Math.signum(direction.x);
      int stepY = (int)Math.signum(direction.y);
      int stepZ = (int)Math.signum(direction.z);
      double nextBoundaryX = stepX > 0 ? currentPos.getX() + 1 : currentPos.getX();
      double nextBoundaryY = stepY > 0 ? currentPos.getY() + 1 : currentPos.getY();
      double nextBoundaryZ = stepZ > 0 ? currentPos.getZ() + 1 : currentPos.getZ();
      double tMaxX = (nextBoundaryX - startPos.x) / direction.x;
      double tMaxY = (nextBoundaryY - startPos.y) / direction.y;
      double tMaxZ = (nextBoundaryZ - startPos.z) / direction.z;
      double tDeltaX = stepX / direction.x;
      double tDeltaY = stepY / direction.y;
      double tDeltaZ = stepZ / direction.z;
      World world = mc.player.getWorld();

      while (startPos.distanceTo(currentPos.toCenterPos()) <= reachDistance) {
         if (!world.isAir(currentPos)) {
            BlockState state = world.getBlockState(currentPos);
            if (!ignorePredicate.test(state)) {
               FluidState fluidState = world.getFluidState(currentPos);
               VoxelShape shape;
               if (fluidState != null && fluidState.isStill() && fluidState.getFluid() == Fluids.WATER) {
                  shape = VoxelShapes.fullCube();
               } else {
                  shape = state.getCollisionShape(world, currentPos, ShapeContext.of(client.player));
               }

               if (!shape.isEmpty()) {
                  for (Box localBox : shape.getBoundingBoxes()) {
                     Box box = localBox.offset(currentPos);
                     Optional<Vec3d> intercept = box.raycast(startPos, endPos);
                     if (intercept.isPresent()) {
                        Vec3d hitVec = intercept.get();
                        Direction side = getHitFaceFromBox(hitVec, box);
                        boolean isInside = box.contains(startPos);
                        return new BlockHitResult(hitVec, side, currentPos, isInside);
                     }
                  }
               }
            }
         }

         if (tMaxX < tMaxY) {
            if (tMaxX < tMaxZ) {
               currentPos = currentPos.add(stepX, 0, 0);
               tMaxX += tDeltaX;
            } else {
               currentPos = currentPos.add(0, 0, stepZ);
               tMaxZ += tDeltaZ;
            }
         } else if (tMaxY < tMaxZ) {
            currentPos = currentPos.add(0, stepY, 0);
            tMaxY += tDeltaY;
         } else {
            currentPos = currentPos.add(0, 0, stepZ);
            tMaxZ += tDeltaZ;
         }
      }

      return null;
   }

   public static boolean isIgnoredBlock(BlockState state) {
      Block block = state.getBlock();
      return block instanceof PlantBlock
         || block instanceof SnowBlock
         || block instanceof AirBlock
         || block instanceof ShortPlantBlock
         || block instanceof FluidBlock;
   }

   private static Direction getHitFaceFromBox(Vec3d hit, Box box) {
      double eps = 1.0E-7;
      if (Math.abs(hit.x - box.minX) <= 1.0E-7) {
         return Direction.WEST;
      } else if (Math.abs(hit.x - box.maxX) <= 1.0E-7) {
         return Direction.EAST;
      } else if (Math.abs(hit.y - box.minY) <= 1.0E-7) {
         return Direction.DOWN;
      } else if (Math.abs(hit.y - box.maxY) <= 1.0E-7) {
         return Direction.UP;
      } else if (Math.abs(hit.z - box.minZ) <= 1.0E-7) {
         return Direction.NORTH;
      } else if (Math.abs(hit.z - box.maxZ) <= 1.0E-7) {
         return Direction.SOUTH;
      } else {
         double dxMin = Math.abs(hit.x - box.minX);
         double dxMax = Math.abs(hit.x - box.maxX);
         double dyMin = Math.abs(hit.y - box.minY);
         double dyMax = Math.abs(hit.y - box.maxY);
         double dzMin = Math.abs(hit.z - box.minZ);
         double dzMax = Math.abs(hit.z - box.maxZ);
         double m = dxMin;
         Direction d = Direction.WEST;
         if (dxMax < dxMin) {
            m = dxMax;
            d = Direction.EAST;
         }

         if (dyMin < m) {
            m = dyMin;
            d = Direction.DOWN;
         }

         if (dyMax < m) {
            m = dyMax;
            d = Direction.UP;
         }

         if (dzMin < m) {
            m = dzMin;
            d = Direction.NORTH;
         }

         if (dzMax < m) {
            d = Direction.SOUTH;
         }

         return d;
      }
   }

   public static BlockHitResult getFacedContainerBlock(float yaw, float pitch) {
      MinecraftClient client = MinecraftClient.getInstance();
      double reachDistance = client.player.getBlockInteractionRange();
      Vec3d startPos = eyePos;
      Vec3d direction = Vec3d.fromPolar(pitch, yaw);
      Vec3d endPos = startPos.add(direction.multiply(reachDistance));
      if (direction.x == 0.0) {
         direction = new Vec3d(1.0E-7, direction.y, direction.z);
      }

      if (direction.y == 0.0) {
         direction = new Vec3d(direction.x, 1.0E-7, direction.z);
      }

      if (direction.z == 0.0) {
         direction = new Vec3d(direction.x, direction.y, 1.0E-7);
      }

      BlockPos currentPos = BlockPos.ofFloored(startPos);
      int stepX = (int)Math.signum(direction.x);
      int stepY = (int)Math.signum(direction.y);
      int stepZ = (int)Math.signum(direction.z);
      double nextBoundaryX = stepX > 0 ? currentPos.getX() + 1 : currentPos.getX();
      double nextBoundaryY = stepY > 0 ? currentPos.getY() + 1 : currentPos.getY();
      double nextBoundaryZ = stepZ > 0 ? currentPos.getZ() + 1 : currentPos.getZ();
      double tMaxX = (nextBoundaryX - startPos.x) / direction.x;
      double tMaxY = (nextBoundaryY - startPos.y) / direction.y;
      double tMaxZ = (nextBoundaryZ - startPos.z) / direction.z;
      double tDeltaX = stepX / direction.x;
      double tDeltaY = stepY / direction.y;
      double tDeltaZ = stepZ / direction.z;

      while (startPos.distanceTo(currentPos.toCenterPos()) <= reachDistance) {
         if (!mc.player.getWorld().isAir(currentPos) && isContainerBlock(mc.player.getWorld().getBlockState(currentPos))) {
            Box currentBox = new Box(currentPos);
            Optional<Vec3d> intercept = currentBox.raycast(startPos, endPos);
            if (intercept.isPresent()) {
               Vec3d hitVec = intercept.get();
               Direction side = getHitFace(hitVec, currentBox);
               boolean isInside = currentBox.contains(startPos);
               return new BlockHitResult(hitVec, side, currentPos, isInside);
            }
         }

         if (tMaxX < tMaxY) {
            if (tMaxX < tMaxZ) {
               currentPos = currentPos.add(stepX, 0, 0);
               tMaxX += tDeltaX;
            } else {
               currentPos = currentPos.add(0, 0, stepZ);
               tMaxZ += tDeltaZ;
            }
         } else if (tMaxY < tMaxZ) {
            currentPos = currentPos.add(0, stepY, 0);
            tMaxY += tDeltaY;
         } else {
            currentPos = currentPos.add(0, 0, stepZ);
            tMaxZ += tDeltaZ;
         }
      }

      return null;
   }

   private static boolean isContainerBlock(BlockState state) {
      return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock || state.getBlock() instanceof ShulkerBoxBlock;
   }

   public static double getDistance(float yaw, float pitch, Entity target) {
      Vec3d dir = Vec3d.fromPolar(pitch, yaw).normalize();
      Box targetBox = target.getBoundingBox();
      return intersectRayAabb(eyePos, dir, targetBox);
   }

   public static boolean didHitEntity(float yaw, float pitch, double range, Entity target) {
      return overBox(yaw, pitch, range, target.getBoundingBox());
   }

   public static boolean overBox(float yawDeg, float pitchDeg, double range, Box box) {
      if (box == null) {
         return false;
      } else {
         Vec3d start = eyePos;
         World world = MinecraftClient.getInstance().world;
         Vec3d dir = Vec3d.fromPolar(pitchDeg, yawDeg).normalize();
         if (dir.lengthSquared() < 1.0E-12) {
            return false;
         } else {
            double tBox = intersectRayAabb(start, dir, box);
            return !Double.isFinite(tBox) || tBox < 0.0 || tBox > range ? false : !isOccludedBefore(world, start, dir, tBox, range, mc.player);
         }
      }
   }

   private static boolean isOccludedBefore(World world, Vec3d origin, Vec3d dir, double tStop, double range, Entity viewer) {
      int x = MathHelper.floor(origin.x);
      int y = MathHelper.floor(origin.y);
      int z = MathHelper.floor(origin.z);
      int stepX = dir.x > 0.0 ? 1 : (dir.x < 0.0 ? -1 : 0);
      int stepY = dir.y > 0.0 ? 1 : (dir.y < 0.0 ? -1 : 0);
      int stepZ = dir.z > 0.0 ? 1 : (dir.z < 0.0 ? -1 : 0);
      double tMaxX = nextBoundaryT(origin.x, dir.x, x, stepX);
      double tMaxY = nextBoundaryT(origin.y, dir.y, y, stepY);
      double tMaxZ = nextBoundaryT(origin.z, dir.z, z, stepZ);
      double tDeltaX = stepX != 0 ? 1.0 / Math.abs(dir.x) : Double.POSITIVE_INFINITY;
      double tDeltaY = stepY != 0 ? 1.0 / Math.abs(dir.y) : Double.POSITIVE_INFINITY;
      double tDeltaZ = stepZ != 0 ? 1.0 / Math.abs(dir.z) : Double.POSITIVE_INFINITY;
      double tLimit = Math.min(tStop, range);
      if (blockOccludesHere(world, origin, dir, new BlockPos(x, y, z), tStop, viewer)) {
         return true;
      } else {
         while (true) {
            if (tMaxX < tMaxY) {
               if (tMaxX < tMaxZ) {
                  if (tMaxX > tLimit) {
                     break;
                  }

                  x += stepX;
                  if (blockOccludesHere(world, origin, dir, new BlockPos(x, y, z), tStop, viewer)) {
                     return true;
                  }

                  tMaxX += tDeltaX;
               } else {
                  if (tMaxZ > tLimit) {
                     break;
                  }

                  z += stepZ;
                  if (blockOccludesHere(world, origin, dir, new BlockPos(x, y, z), tStop, viewer)) {
                     return true;
                  }

                  tMaxZ += tDeltaZ;
               }
            } else if (tMaxY < tMaxZ) {
               if (tMaxY > tLimit) {
                  break;
               }

               y += stepY;
               if (blockOccludesHere(world, origin, dir, new BlockPos(x, y, z), tStop, viewer)) {
                  return true;
               }

               tMaxY += tDeltaY;
            } else {
               if (tMaxZ > tLimit) {
                  break;
               }

               z += stepZ;
               if (blockOccludesHere(world, origin, dir, new BlockPos(x, y, z), tStop, viewer)) {
                  return true;
               }

               tMaxZ += tDeltaZ;
            }
         }

         return false;
      }
   }

   private static double nextBoundaryT(double o, double d, int cell, int step) {
      if (step == 0) {
         return Double.POSITIVE_INFINITY;
      } else {
         double boundary = step > 0 ? cell + 1 : cell;
         return (boundary - o) / d;
      }
   }

   private static boolean blockOccludesHere(World world, Vec3d origin, Vec3d dir, BlockPos pos, double tEntity, Entity viewer) {
      BlockState state = world.getBlockState(pos);
      if (state.isAir()) {
         return false;
      } else {
         VoxelShape shape = state.getOutlineShape(world, pos, viewer != null ? ShapeContext.of(viewer) : ShapeContext.absent());
         if (shape.isEmpty()) {
            return false;
         } else {
            for (Box local : shape.getBoundingBoxes()) {
               Box box = local.offset(pos);
               double tBox = intersectRayAabb(origin, dir, box);
               if (Double.isFinite(tBox) && tBox >= 0.0 && tBox < tEntity) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static double intersectRayAabb(Vec3d o, Vec3d d, Box b) {
      double tMin = 0.0;
      double tMax = Double.POSITIVE_INFINITY;
      if (!axisSlab(o.x, d.x, b.minX, b.maxX, ClientRayTraceUtil.Holder.tmp)) {
         return Double.POSITIVE_INFINITY;
      } else {
         tMin = Math.max(tMin, ClientRayTraceUtil.Holder.tmp[0]);
         tMax = Math.min(tMax, ClientRayTraceUtil.Holder.tmp[1]);
         if (tMax < tMin) {
            return Double.POSITIVE_INFINITY;
         } else if (!axisSlab(o.y, d.y, b.minY, b.maxY, ClientRayTraceUtil.Holder.tmp)) {
            return Double.POSITIVE_INFINITY;
         } else {
            tMin = Math.max(tMin, ClientRayTraceUtil.Holder.tmp[0]);
            tMax = Math.min(tMax, ClientRayTraceUtil.Holder.tmp[1]);
            if (tMax < tMin) {
               return Double.POSITIVE_INFINITY;
            } else if (!axisSlab(o.z, d.z, b.minZ, b.maxZ, ClientRayTraceUtil.Holder.tmp)) {
               return Double.POSITIVE_INFINITY;
            } else {
               tMin = Math.max(tMin, ClientRayTraceUtil.Holder.tmp[0]);
               tMax = Math.min(tMax, ClientRayTraceUtil.Holder.tmp[1]);
               return tMax < tMin ? Double.POSITIVE_INFINITY : tMin;
            }
         }
      }
   }

   private static boolean axisSlab(double o, double d, double min, double max, double[] out) {
      if (Math.abs(d) < 1.0E-12) {
         if (!(o < min) && !(o > max)) {
            out[0] = Double.NEGATIVE_INFINITY;
            out[1] = Double.POSITIVE_INFINITY;
            return true;
         } else {
            return false;
         }
      } else {
         double inv = 1.0 / d;
         double t1 = (min - o) * inv;
         double t2 = (max - o) * inv;
         if (t1 > t2) {
            double tmp = t1;
            t1 = t2;
            t2 = tmp;
         }

         out[0] = t1;
         out[1] = t2;
         return true;
      }
   }

   private static final class Holder {
      static final double[] tmp = new double[2];
   }
}
