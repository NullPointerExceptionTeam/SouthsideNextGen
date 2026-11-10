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
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class SearchUtils implements MinecraftAccessor {
   public boolean isAirBlock(BlockPos blockPos) {
      return mc.world != null && mc.world.getBlockState(blockPos).isAir();
   }

   public BlockData getBlockData(Vec3d player, float posY) {
      return this.isAirBlock(
            new BlockPos((int)Math.floor(player.getX()), (int)Math.floor(player.getY()), (int)Math.floor(player.getZ()))
         )
         ? this.getBlockData(new BlockPos((int)Math.floor(player.getX()), (int)posY, (int)Math.floor(player.getZ())), player)
         : null;
   }

   private BlockData getBlockData(BlockPos pos, Vec3d player) {
      BlockData data;
      if (this.getPos(pos) == null) {
         BlockPos blockPos = this.getBlockPos(player);
         if (blockPos == null) {
            return null;
         }

         Direction direction = this.getPlaceSide(blockPos, player);
         if (direction == null) {
            return null;
         }

         data = new BlockData(blockPos, direction);
      } else {
         data = this.getPos(pos);
      }

      return this.isAirBlock(data.pos().offset(data.facing())) ? data : null;
   }

   private Direction getPlaceSide(BlockPos blockPos, Vec3d player) {
      List<BlockData> blockData = new ArrayList<>();
      BlockPos pos = new BlockPos((int)Math.floor(player.getX()), (int)Math.floor(player.getY()), (int)Math.floor(player.getZ()));
      if (this.isAirBlock(blockPos.east()) && !blockPos.east().equals(pos)) {
         blockData.add(new BlockData(blockPos.east(), Direction.EAST));
      }

      if (this.isAirBlock(blockPos.north()) && !blockPos.north().equals(pos)) {
         blockData.add(new BlockData(blockPos.north(), Direction.NORTH));
      }

      if (this.isAirBlock(blockPos.south()) && !blockPos.south().equals(pos)) {
         blockData.add(new BlockData(blockPos.south(), Direction.SOUTH));
      }

      if (this.isAirBlock(blockPos.west()) && !blockPos.west().equals(pos)) {
         blockData.add(new BlockData(blockPos.west(), Direction.WEST));
      }

      if (blockData.isEmpty()) {
         return null;
      } else {
         blockData.sort(Comparator.comparingDouble(vec3 -> vec3.pos().getSquaredDistance(pos)));
         blockData.removeIf(blockData1 -> !mc.world.getBlockState(blockData1.pos().offset(blockData1.facing())).isAir());
         return blockData.get(0).facing();
      }
   }

   private BlockPos getBlockPos(Vec3d player) {
      BlockPos playerPos = new BlockPos(
         (int)Math.floor(player.getX()), (int)Math.floor(player.getY()), (int)Math.floor(player.getZ())
      );
      ArrayList<BlockPos> positions = new ArrayList<>();
      Map<BlockPos, Block> searchBlock = this.searchBlocks(5, player);

      for (Entry<BlockPos, Block> block : searchBlock.entrySet()) {
         if (this.isPosSolid(block.getKey())) {
            positions.add(block.getKey());
         }
      }

      positions.removeIf(pos -> this.distanceTo(player, pos) > mc.player.getBlockInteractionRange() || pos.getY() >= playerPos.getY());
      if (positions.isEmpty()) {
         return null;
      } else {
         positions.sort(Comparator.comparingDouble(vec3 -> vec3.getSquaredDistance(playerPos)));
         return positions.get(0);
      }
   }

   public float distanceTo(Vec3d entityPos, BlockPos pos) {
      float f = (float)(entityPos.getX() - pos.getX());
      float g = (float)(entityPos.getY() - pos.getY());
      float h = (float)(entityPos.getZ() - pos.getZ());
      return MathHelper.sqrt(f * f + g * g + h * h);
   }

   public BlockData getPos(BlockPos pos) {
      if (this.isPosSolid(pos.add(-1, 0, 0))) {
         return new BlockData(pos.add(-1, 0, 0), Direction.EAST);
      } else if (this.isPosSolid(pos.add(1, 0, 0))) {
         return new BlockData(pos.add(1, 0, 0), Direction.WEST);
      } else if (this.isPosSolid(pos.add(0, 0, 1))) {
         return new BlockData(pos.add(0, 0, 1), Direction.NORTH);
      } else if (this.isPosSolid(pos.add(0, 0, -1))) {
         return new BlockData(pos.add(0, 0, -1), Direction.SOUTH);
      } else {
         return this.isPosSolid(pos.add(0, -1, 0)) ? new BlockData(pos.add(0, -1, 0), Direction.UP) : null;
      }
   }

   public Map<BlockPos, Block> searchBlocks(int radius, Vec3d player) {
      Map<BlockPos, Block> blocks = new HashMap<>();
      if (player == null) {
         return blocks;
      } else {
         for (int x = radius; x >= -radius + 1; x--) {
            for (int y = radius; y >= -radius + 1; y--) {
               for (int z = radius; z >= -radius + 1; z--) {
                  BlockPos blockPos = new BlockPos((int)player.getX() + x, (int)player.getY() + y, (int)player.getZ() + z);
                  Block block = this.getBlock(blockPos);
                  if (block != null) {
                     blocks.put(blockPos, block);
                  }
               }
            }
         }

         return blocks;
      }
   }

   public Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   public boolean isPosSolid(BlockPos pos) {
      Block block = mc.world.getBlockState(pos).getBlock();
      return !Arrays.asList(
            Blocks.ANVIL,
            Blocks.AIR,
            Blocks.WATER,
            Blocks.FIRE,
            Blocks.LAVA,
            Blocks.SKELETON_SKULL,
            Blocks.OAK_SIGN,
            Blocks.TRAPPED_CHEST,
            Blocks.CHEST,
            Blocks.ENCHANTING_TABLE,
            Blocks.ENDER_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.COBWEB
         )
         .contains(block);
   }
}
