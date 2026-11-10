package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.misc.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.TntBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.FurnaceBlock;

public final class BlockUtils {
   public static boolean isSolidBlockUnder(double height) {
      for (double y = 0.0; y < height; y++) {
         BlockPos pos = BlockPos.ofFloored(
            MinecraftAccessor.mc.player.getX(), MinecraftAccessor.mc.player.getY() - y, MinecraftAccessor.mc.player.getZ()
         );
         BlockState state = MinecraftAccessor.mc.world.getBlockState(pos);
         if (!state.isAir()
            && !MinecraftAccessor.mc
               .world
               .getBlockCollisions(
                  MinecraftAccessor.mc.player,
                  new Box(
                     pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
                  )
               )
               .iterator()
               .hasNext()) {
            return true;
         }
      }

      return false;
   }

   public static boolean isValidBlock(Vec3d block) {
      return isValidBlock(
         MinecraftAccessor.mc.world.getBlockState(new BlockPos((int)block.x, (int)block.y, (int)block.z)).getBlock()
      );
   }

   public static boolean isValidBlock(Block block) {
      return !(block instanceof FluidBlock)
         && !(block instanceof AirBlock)
         && !(block instanceof ChestBlock)
         && !(block instanceof FurnaceBlock)
         && !(block instanceof LadderBlock)
         && !(block instanceof TntBlock);
   }

   public static boolean isStandingSafely() {
      if (MinecraftAccessor.mc.player != null && MinecraftAccessor.mc.world != null) {
         Vec3d playerPos = MinecraftAccessor.mc.player.getPos();
         BlockPos centralPointBehind = new BlockPos(
            (int)Math.floor(playerPos.x + Math.sin(Math.toRadians(MinecraftAccessor.mc.player.getYaw())) * 0.8),
            (int)Math.floor(playerPos.y),
            (int)Math.floor(playerPos.z + -Math.cos(Math.toRadians(MinecraftAccessor.mc.player.getYaw())) * 0.8)
         );
         if (MinecraftAccessor.mc.world.getBlockState(centralPointBehind).getBlock() == Blocks.LAVA) {
            return false;
         } else {
            BlockPos pos = centralPointBehind.down();
            BlockState state = MinecraftAccessor.mc.world.getBlockState(pos);
            if (state.getBlock() == Blocks.LAVA) {
               return false;
            } else {
               if (state.isAir()) {
                  if (pos.getY() < MinecraftAccessor.mc.world.getBottomY()) {
                     return false;
                  }

                  if (MinecraftAccessor.mc.world.getBlockState(pos.down()).isAir()) {
                     return pos.down().getY() >= MinecraftAccessor.mc.world.getBottomY()
                        && !MinecraftAccessor.mc.world.getBlockState(pos.down().down()).isAir();
                  }
               }

               return true;
            }
         }
      } else {
         return true;
      }
   }

   public static List<Pair<BlockPos, Block>> getBlocksInRadius(int radius) {
      ArrayList<Pair<BlockPos, Block>> blocks = new ArrayList<>();
      if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
         for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
               for (int z = -radius; z <= radius; z++) {
                  double x1 = MinecraftClient.getInstance().player.getX();
                  double y1 = MinecraftClient.getInstance().player.getY();
                  double z1 = MinecraftClient.getInstance().player.getZ();
                  BlockPos blockPos = new BlockPos((int)(x + x1), (int)(y + y1), (int)(z + z1));
                  blocks.add(new Pair<>(blockPos, MinecraftClient.getInstance().world.getBlockState(blockPos).getBlock()));
               }
            }
         }

         return blocks;
      } else {
         return null;
      }
   }
}
