package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.types.Rotation;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class ProjectileUtil {
   public static ProjectileUtil.ProjectileHit predict(
      double posX, double posY, double posZ, double motionX, double motionY, double motionZ, double motionSlowdown, double size, double gravity, boolean draw
   ) {
      HitResult landingPosition = null;
      boolean hasLanded = false;
      boolean hitEntity = false;
      int ticks = 0;

      while (!hasLanded && posY > -60.0) {
         ticks++;
         Vec3d posBefore = new Vec3d(posX, posY, posZ);
         Vec3d posAfter = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
         BlockHitResult blockHitResult = MinecraftAccessor.mc
            .world
            .raycast(new RaycastContext(posBefore, posAfter, ShapeType.COLLIDER, FluidHandling.ANY, MinecraftAccessor.mc.player));
         if (blockHitResult != null && blockHitResult.getType() == Type.BLOCK) {
            hasLanded = true;
            landingPosition = blockHitResult;
            posAfter = blockHitResult.getPos();
         }

         Box arrowBox = new Box(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);

         for (Entity entity : MinecraftAccessor.mc.world.getOtherEntities(null, arrowBox.expand(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0))) {
            if (entity.canHit() && entity != MinecraftAccessor.mc.player) {
               Box var2 = entity.getBoundingBox().expand(size, size, size);
               Optional<Vec3d> possibleEntityLanding = var2.raycast(posBefore, posAfter);
               if (possibleEntityLanding.isPresent()) {
                  hitEntity = true;
                  hasLanded = true;
                  landingPosition = new BlockHitResult(possibleEntityLanding.get(), null, BlockPos.ORIGIN, false);
               }
            }
         }

         posX += motionX;
         posY += motionY;
         posZ += motionZ;
         BlockPos var35 = new BlockPos((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
         BlockState var36 = MinecraftAccessor.mc.world.getBlockState(var35);
         if (var36.isOf(Blocks.WATER)) {
            motionX *= 0.6;
            motionY *= 0.6;
            motionZ *= 0.6;
         } else {
            motionX *= motionSlowdown;
            motionY *= motionSlowdown;
            motionZ *= motionSlowdown;
         }

         motionY -= gravity;
      }

      return new ProjectileUtil.ProjectileHit(posX, posY, posZ, hitEntity, hasLanded, (BlockHitResult)landingPosition, ticks);
   }

   public static class EnderPearlPredictor {
      public double predictX;
      public double predictY;
      public double predictZ;
      public double minMotionY;
      public double maxMotionY;

      public EnderPearlPredictor(double predictX, double predictY, double predictZ, double minMotionY, double maxMotionY) {
         this.predictX = predictX;
         this.predictY = predictY;
         this.predictZ = predictZ;
         this.minMotionY = minMotionY;
         this.maxMotionY = maxMotionY;
      }

      public double assessRotation(Rotation rotation) {
         double sum = 0.0;
         int cnt = 0;

         for (int rateIndex = 0; rateIndex <= 3; rateIndex++) {
            double rate = rateIndex / 3.0;

            for (int yaw = -1; yaw <= 1; yaw++) {
               for (int pitch = -1; pitch <= 1; pitch++) {
                  sum += this.assessSingleRotation(
                     new Rotation(rotation.yaw + yaw * 0.5F, rotation.pitch + pitch * 0.5F), MathUtils.interpolate(this.minMotionY, this.maxMotionY, rate)
                  );
                  cnt++;
               }
            }

            if (this.minMotionY == this.maxMotionY) {
               break;
            }
         }

         return sum / cnt;
      }

      public double assessSingleRotation(Rotation rotation, double motionYOffset) {
         return this.assessSingleRotation(rotation, motionYOffset, false);
      }

      public double assessSingleRotation(Rotation rotation, double motionYOffset, boolean debug) {
         if (rotation.pitch > 90.0F) {
            rotation.pitch = 90.0F;
         }

         if (rotation.pitch < -90.0F) {
            rotation.pitch = -90.0F;
         }

         float motionFactor = 1.5F;
         float gravity = 0.03F;
         float size = 0.25F;
         float motionSlowdown = 0.99F;
         double posX = this.predictX - MathHelper.cos(rotation.yaw / 180.0F * (float) Math.PI) * 0.16F;
         double posY = this.predictY + MinecraftAccessor.mc.player.getEyeHeight(MinecraftAccessor.mc.player.getPose()) - 0.1F;
         double posZ = this.predictZ - MathHelper.sin(rotation.yaw / 180.0F * (float) Math.PI) * 0.16F;
         double motionX = -MathHelper.sin(rotation.yaw / 180.0F * (float) Math.PI)
               * MathHelper.cos(rotation.pitch / 180.0F * (float) Math.PI)
            * 0.4;
         double motionY = -MathHelper.sin(rotation.pitch / 180.0F * (float) Math.PI) * 0.4;
         double motionZ = MathHelper.cos(rotation.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotation.pitch / 180.0F * (float) Math.PI)
            * 0.4;
         float distance = MathHelper.sqrt((float)(motionX * motionX + motionY * motionY + motionZ * motionZ));
         motionX /= distance;
         motionY /= distance;
         motionZ /= distance;
         motionX *= 1.5;
         motionY *= 1.5;
         motionZ *= 1.5;
         motionY += motionYOffset;
         ProjectileUtil.ProjectileHit projectileHit = ProjectileUtil.predict(posX, posY, posZ, motionX, motionY, motionZ, 0.99F, 0.25, 0.03F, false);
         if (!projectileHit.hasLanded()) {
            if (debug) {
               ChatUtils.info(
                  String.format(
                     "§7[assess] yaw=%.0f pitch=%.0f → §cNO LANDING§7 endPos=(%.0f, %.0f, %.0f) ticks=%d",
                     rotation.yaw,
                     rotation.pitch,
                     projectileHit.posX(),
                     projectileHit.posY(),
                     projectileHit.posZ(),
                     projectileHit.ticks()
                  )
               );
            }

            return 0.05;
         } else {
            BlockHitResult hitBlock = projectileHit.landingPosition();
            if (hitBlock instanceof BlockHitResult) {
               BlockPos hitBlockx = hitBlock.getBlockPos();
               BlockPos landPos = hitBlockx.offset(hitBlock.getSide());
               Direction side = hitBlock.getSide();
               double score;
               if (side == Direction.UP) {
                  score = this.assessBlockPos(landPos) * this.distanceFunction(projectileHit.ticks);
               } else {
                  double nearbyScore = this.assessNearbyGround(hitBlockx, landPos);
                  score = nearbyScore * this.distanceFunction(projectileHit.ticks);
               }

               if (debug) {
                  ChatUtils.info(
                     String.format(
                        "§7[assess] yaw=%.0f pitch=%.0f → §a%s§7 at (%d,%d,%d) ticks=%d finalScore=%.3f",
                        rotation.yaw,
                        rotation.pitch,
                        side == Direction.UP ? "TOP" : side.name(),
                        landPos.getX(),
                        landPos.getY(),
                        landPos.getZ(),
                        projectileHit.ticks(),
                        score
                     )
                  );
               }

               return score;
            } else {
               if (debug) {
                  ChatUtils.info(String.format("§7[assess] yaw=%.0f pitch=%.0f → §eHIT ENTITY§7 ticks=%d", rotation.yaw, rotation.pitch, projectileHit.ticks()));
               }

               return 0.05 * this.distanceFunction(projectileHit.ticks);
            }
         }
      }

      private double assessBlockPos(BlockPos pos) {
         for (int y = 0; y >= -5; y--) {
            BlockPos checkPos = pos.add(0, y, 0);
            BlockState blockState = MinecraftAccessor.mc.world.getBlockState(checkPos);
            if (blockState.isSolidBlock(MinecraftAccessor.mc.world, checkPos)) {
               return y == 0 ? 0.4 : 1.0;
            }

            if (blockState.isOpaque()) {
               return 0.99;
            }
         }

         return 0.05;
      }

      private double assessNearbyGround(BlockPos hitBlock, BlockPos landPos) {
         if (MinecraftAccessor.mc.world.getBlockState(hitBlock).isSolidBlock(MinecraftAccessor.mc.world, hitBlock)
            && MinecraftAccessor.mc.world.getBlockState(hitBlock.up()).isAir()) {
            return 0.85;
         } else if (MinecraftAccessor.mc.world.getBlockState(hitBlock).isSolidBlock(MinecraftAccessor.mc.world, hitBlock)) {
            for (int y = 1; y <= 5; y++) {
               BlockPos above = hitBlock.up(y);
               if (MinecraftAccessor.mc.world.getBlockState(above).isAir()) {
                  return 0.75;
               }
            }

            return 0.6;
         } else {
            for (int yx = 0; yx >= -5; yx--) {
               BlockPos below = landPos.add(0, yx, 0);
               if (MinecraftAccessor.mc.world.getBlockState(below).isSolidBlock(MinecraftAccessor.mc.world, below)) {
                  return yx == 0 ? 0.3 : 0.7;
               }
            }

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dz != 0) {
                     for (int dy = -1; dy <= 2; dy++) {
                        BlockPos nearby = landPos.add(dx, dy, dz);
                        if (MinecraftAccessor.mc.world.getBlockState(nearby).isSolidBlock(MinecraftAccessor.mc.world, nearby)) {
                           return 0.55;
                        }
                     }
                  }
               }
            }

            return 0.4;
         }
      }

      private double distanceFunction(double d) {
         d /= 1000.0;
         return (d + 3.0) / (d + 2.0) / 1.5;
      }
   }

   public record ProjectileHit(double posX, double posY, double posZ, boolean hitEntity, boolean hasLanded, BlockHitResult landingPosition, int ticks) {
   }
}
