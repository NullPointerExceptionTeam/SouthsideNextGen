package dev.southside.module.impl.world.sca;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.player.DirectionalInput;
import dev.southside.utils.world.EntityUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class ScaffoldMovementPlanner {
   private static final int MAX_LAST_PLACE_BLOCKS = 4;
   private final Deque<class_2338> lastPlacedBlocks = new ArrayDeque<>(4);
   private class_2338 lastPosition = null;

   public Line getOptimalMovementLine(DirectionalInput directionalInput) {
      class_243 direction = this.chooseDirection(EntityUtils.getMovementDirectionOfInput(MinecraftAccessor.mc.field_1724.method_36454(), directionalInput));
      class_2338 blockUnderPlayer = this.findBlockPlayerStandsOn();
      if (blockUnderPlayer == null) {
         return null;
      } else {
         Line lastBlocksLine = this.fitLinesThroughLastPlacedBlocks();
         class_243 lineBaseBlock;
         if (lastBlocksLine != null && !this.divergesTooMuchFromDirection(lastBlocksLine, direction)) {
            lineBaseBlock = lastBlocksLine.position;
         } else {
            lineBaseBlock = class_243.method_24955(blockUnderPlayer);
         }

         return new Line(
            new class_243(lineBaseBlock.field_1352 + 0.5, MinecraftAccessor.mc.field_1724.method_23318(), lineBaseBlock.field_1350 + 0.5), direction
         );
      }
   }

   private boolean divergesTooMuchFromDirection(Line lastBlocksLine, class_243 direction) {
      double dot = lastBlocksLine.direction.method_1026(direction);
      double angleRad = Math.acos(class_3532.method_15350(dot, -1.0, 1.0));
      double angleDeg = Math.toDegrees(angleRad);
      return Math.abs(angleDeg) > 50.0;
   }

   private Line fitLinesThroughLastPlacedBlocks() {
      List<class_2338> lastPlacedBlocksToConsider = new ArrayList<>(this.lastPlacedBlocks);
      int size = lastPlacedBlocksToConsider.size();
      if (size < 2) {
         return null;
      } else {
         class_2338 block0 = lastPlacedBlocksToConsider.get(size - 2);
         class_2338 block1 = lastPlacedBlocksToConsider.get(size - 1);
         class_243 avgPos = new class_243(
            (block0.method_10263() + block1.method_10263()) / 2.0,
            (block0.method_10264() + block1.method_10264()) / 2.0,
            (block0.method_10260() + block1.method_10260()) / 2.0
         );
         class_243 dir = new class_243(
               block1.method_10263() - block0.method_10263(), block1.method_10264() - block0.method_10264(), block1.method_10260() - block0.method_10260()
            )
            .method_1029();
         return new Line(avgPos, dir);
      }
   }

   private class_2338 findBlockPlayerStandsOn() {
      double[] offsetsToTry = new double[]{0.301, 0.0, -0.301};
      List<class_2338> candidates = new ArrayList<>();

      for (double xOffset : offsetsToTry) {
         for (double zOffset : offsetsToTry) {
            class_2338 playerPos = new class_2338(
               (int)Math.floor(MinecraftAccessor.mc.field_1724.method_23317() + xOffset),
               (int)Math.floor(MinecraftAccessor.mc.field_1724.method_23318() - 1.0),
               (int)Math.floor(MinecraftAccessor.mc.field_1724.method_23321() + zOffset)
            );
            if (!MinecraftAccessor.mc.field_1687.method_8320(playerPos).method_26220(MinecraftAccessor.mc.field_1687, playerPos).method_1110()) {
               candidates.add(playerPos);
            }
         }
      }

      class_2338 lastPlacedBlock = this.lastPlacedBlocks.peekLast();
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

   private class_243 chooseDirection(float currentAngle) {
      double currentDirection = currentAngle / 180.0 * 4.0 + 4.0;
      double newDirectionNumber = Math.round(currentDirection);
      double newDirectionAngle = Math.toRadians(class_3532.method_15338((newDirectionNumber - 4.0) / 4.0 * 180.0 + 90.0));
      return new class_243(Math.cos(newDirectionAngle), 0.0, Math.sin(newDirectionAngle));
   }

   public void trackPlacedBlock(class_2338 target) {
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
