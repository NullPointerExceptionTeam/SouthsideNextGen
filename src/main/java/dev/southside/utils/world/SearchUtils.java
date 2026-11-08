package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.BlockData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class SearchUtils implements MinecraftAccessor {
   public boolean isAirBlock(class_2338 blockPos) {
      return mc.field_1687 != null && mc.field_1687.method_8320(blockPos).method_26215();
   }

   public BlockData getBlockData(class_243 player, float posY) {
      return this.isAirBlock(
            new class_2338((int)Math.floor(player.method_10216()), (int)Math.floor(player.method_10214()), (int)Math.floor(player.method_10215()))
         )
         ? this.getBlockData(new class_2338((int)Math.floor(player.method_10216()), (int)posY, (int)Math.floor(player.method_10215())), player)
         : null;
   }

   private BlockData getBlockData(class_2338 pos, class_243 player) {
      BlockData data;
      if (this.getPos(pos) == null) {
         class_2338 blockPos = this.getBlockPos(player);
         if (blockPos == null) {
            return null;
         }

         class_2350 direction = this.getPlaceSide(blockPos, player);
         if (direction == null) {
            return null;
         }

         data = new BlockData(blockPos, direction);
      } else {
         data = this.getPos(pos);
      }

      return this.isAirBlock(data.pos().method_10093(data.facing())) ? data : null;
   }

   private class_2350 getPlaceSide(class_2338 blockPos, class_243 player) {
      List<BlockData> blockData = new ArrayList<>();
      class_2338 pos = new class_2338((int)Math.floor(player.method_10216()), (int)Math.floor(player.method_10214()), (int)Math.floor(player.method_10215()));
      if (this.isAirBlock(blockPos.method_10078()) && !blockPos.method_10078().equals(pos)) {
         blockData.add(new BlockData(blockPos.method_10078(), class_2350.field_11034));
      }

      if (this.isAirBlock(blockPos.method_10095()) && !blockPos.method_10095().equals(pos)) {
         blockData.add(new BlockData(blockPos.method_10095(), class_2350.field_11043));
      }

      if (this.isAirBlock(blockPos.method_10072()) && !blockPos.method_10072().equals(pos)) {
         blockData.add(new BlockData(blockPos.method_10072(), class_2350.field_11035));
      }

      if (this.isAirBlock(blockPos.method_10067()) && !blockPos.method_10067().equals(pos)) {
         blockData.add(new BlockData(blockPos.method_10067(), class_2350.field_11039));
      }

      if (blockData.isEmpty()) {
         return null;
      } else {
         blockData.sort(Comparator.comparingDouble(vec3 -> vec3.pos().method_10262(pos)));
         blockData.removeIf(blockData1 -> !mc.field_1687.method_8320(blockData1.pos().method_10093(blockData1.facing())).method_26215());
         return blockData.get(0).facing();
      }
   }

   private class_2338 getBlockPos(class_243 player) {
      class_2338 playerPos = new class_2338(
         (int)Math.floor(player.method_10216()), (int)Math.floor(player.method_10214()), (int)Math.floor(player.method_10215())
      );
      ArrayList<class_2338> positions = new ArrayList<>();
      Map<class_2338, class_2248> searchBlock = this.searchBlocks(5, player);

      for (Entry<class_2338, class_2248> block : searchBlock.entrySet()) {
         if (this.isPosSolid(block.getKey())) {
            positions.add(block.getKey());
         }
      }

      positions.removeIf(pos -> this.distanceTo(player, pos) > mc.field_1724.method_55754() || pos.method_10264() >= playerPos.method_10264());
      if (positions.isEmpty()) {
         return null;
      } else {
         positions.sort(Comparator.comparingDouble(vec3 -> vec3.method_10262(playerPos)));
         return positions.get(0);
      }
   }

   public float distanceTo(class_243 entityPos, class_2338 pos) {
      float f = (float)(entityPos.method_10216() - pos.method_10263());
      float g = (float)(entityPos.method_10214() - pos.method_10264());
      float h = (float)(entityPos.method_10215() - pos.method_10260());
      return class_3532.method_15355(f * f + g * g + h * h);
   }

   public BlockData getPos(class_2338 pos) {
      if (this.isPosSolid(pos.method_10069(-1, 0, 0))) {
         return new BlockData(pos.method_10069(-1, 0, 0), class_2350.field_11034);
      } else if (this.isPosSolid(pos.method_10069(1, 0, 0))) {
         return new BlockData(pos.method_10069(1, 0, 0), class_2350.field_11039);
      } else if (this.isPosSolid(pos.method_10069(0, 0, 1))) {
         return new BlockData(pos.method_10069(0, 0, 1), class_2350.field_11043);
      } else if (this.isPosSolid(pos.method_10069(0, 0, -1))) {
         return new BlockData(pos.method_10069(0, 0, -1), class_2350.field_11035);
      } else {
         return this.isPosSolid(pos.method_10069(0, -1, 0)) ? new BlockData(pos.method_10069(0, -1, 0), class_2350.field_11036) : null;
      }
   }

   public Map<class_2338, class_2248> searchBlocks(int radius, class_243 player) {
      Map<class_2338, class_2248> blocks = new HashMap<>();
      if (player == null) {
         return blocks;
      } else {
         for (int x = radius; x >= -radius + 1; x--) {
            for (int y = radius; y >= -radius + 1; y--) {
               for (int z = radius; z >= -radius + 1; z--) {
                  class_2338 blockPos = new class_2338((int)player.method_10216() + x, (int)player.method_10214() + y, (int)player.method_10215() + z);
                  class_2248 block = this.getBlock(blockPos);
                  if (block != null) {
                     blocks.put(blockPos, block);
                  }
               }
            }
         }

         return blocks;
      }
   }

   public class_2248 getBlock(class_2338 pos) {
      return mc.field_1687.method_8320(pos).method_26204();
   }

   public boolean isPosSolid(class_2338 pos) {
      class_2248 block = mc.field_1687.method_8320(pos).method_26204();
      return !Arrays.asList(
            class_2246.field_10535,
            class_2246.field_10124,
            class_2246.field_10382,
            class_2246.field_10036,
            class_2246.field_10164,
            class_2246.field_10481,
            class_2246.field_10121,
            class_2246.field_10380,
            class_2246.field_10034,
            class_2246.field_10485,
            class_2246.field_10443,
            class_2246.field_9980,
            class_2246.field_10343
         )
         .contains(block);
   }
}
