package dev.southside.module.impl.world.sca;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.player.DirectionalInput;
import dev.southside.utils.world.EntityUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class ScaffoldMovementPlanner {
   private static final int MAX_LAST_PLACE_BLOCKS = 4;
   private final Deque<BlockPos> lastPlacedBlocks = new ArrayDeque<>(4);
   private BlockPos lastPosition = null;

   public Line getOptimalMovementLine(DirectionalInput directionalInput) {
      Vec3d direction = this.chooseDirection(EntityUtils.getMovementDirectionOfInput(MinecraftAccessor.mc.player.getYaw(), directionalInput));
      BlockPos blockUnderPlayer = this.findBlockPlayerStandsOn();
      if (blockUnderPlayer == null) {
         return null;
      } else {
         Line lastBlocksLine = this.fitLinesThroughLastPlacedBlocks();
         Vec3d lineBaseBlock;
         if (lastBlocksLine != null && !this.divergesTooMuchFromDirection(lastBlocksLine, direction)) {
            lineBaseBlock = lastBlocksLine.position;
         } else {
            lineBaseBlock = Vec3d.ofBottomCenter(blockUnderPlayer);
         }

         return new Line(
            new Vec3d(lineBaseBlock.x + 0.5, MinecraftAccessor.mc.player.getY(), lineBaseBlock.z + 0.5), direction
         );
      }
   }

   private boolean divergesTooMuchFromDirection(Line lastBlocksLine, Vec3d direction) {
      double dot = lastBlocksLine.direction.dotProduct(direction);
      double angleRad = Math.acos(MathHelper.clamp(dot, -1.0, 1.0));
      double angleDeg = Math.toDegrees(angleRad);
      return Math.abs(angleDeg) > 50.0;
   }

   private Line fitLinesThroughLastPlacedBlocks() {
      List<BlockPos> lastPlacedBlocksToConsider = new ArrayList<>(this.lastPlacedBlocks);
      int size = lastPlacedBlocksToConsider.size();
      if (size < 2) {
         return null;
      } else {
         BlockPos block0 = lastPlacedBlocksToConsider.get(size - 2);
         BlockPos block1 = lastPlacedBlocksToConsider.get(size - 1);
         Vec3d avgPos = new Vec3d(
            (block0.getX() + block1.getX()) / 2.0,
            (block0.getY() + block1.getY()) / 2.0,
            (block0.getZ() + block1.getZ()) / 2.0
         );
         Vec3d dir = new Vec3d(
               block1.getX() - block0.getX(), block1.getY() - block0.getY(), block1.getZ() - block0.getZ()
            )
            .normalize();
         return new Line(avgPos, dir);
      }
   }

   private BlockPos findBlockPlayerStandsOn() {
      double[] offsetsToTry = new double[]{0.301, 0.0, -0.301};
      List<BlockPos> candidates = new ArrayList<>();

      for (double xOffset : offsetsToTry) {
         for (double zOffset : offsetsToTry) {
            BlockPos playerPos = new BlockPos(
               (int)Math.floor(MinecraftAccessor.mc.player.getX() + xOffset),
               (int)Math.floor(MinecraftAccessor.mc.player.getY() - 1.0),
               (int)Math.floor(MinecraftAccessor.mc.player.getZ() + zOffset)
            );
            if (!MinecraftAccessor.mc.world.getBlockState(playerPos).getCollisionShape(MinecraftAccessor.mc.world, playerPos).isEmpty()) {
               candidates.add(playerPos);
            }
         }
      }

      BlockPos lastPlacedBlock = this.lastPlacedBlocks.peekLast();
      if (lastPlacedBlock != null && candidates.contains(lastPlacedBlock)) {
         return lastPlacedBlock;
      } else if (this.lastPosition != null && candidates.contains(this.lastPosition)) {
         return this.lastPosition;
      } else if (!candidates.isEmpty()) {
         this.lastPosition = candidates.get(0);
         return this.lastPosition;
      } else {
         return null;
      }
   }

   private Vec3d chooseDirection(float currentAngle) {
      double currentDirection = currentAngle / 180.0 * 4.0 + 4.0;
      double newDirectionNumber = Math.round(currentDirection);
      double newDirectionAngle = Math.toRadians(MathHelper.wrapDegrees((newDirectionNumber - 4.0) / 4.0 * 180.0 + 90.0));
      return new Vec3d(Math.cos(newDirectionAngle), 0.0, Math.sin(newDirectionAngle));
   }

   public void trackPlacedBlock(BlockPos target) {
      this.lastPlacedBlocks.add(target);

      while (this.lastPlacedBlocks.size() > 4) {
         this.lastPlacedBlocks.pollFirst();
      }
   }

   public void reset() {
      this.lastPosition = null;
      this.lastPlacedBlocks.clear();
   }
}
