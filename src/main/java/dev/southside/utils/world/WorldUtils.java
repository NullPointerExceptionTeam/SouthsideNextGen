package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.entity.BlockEntity;

public class WorldUtils {
   public static Iterable<BlockEntity> blockEntities() {
      return BlockEntityIterator::new;
   }

   public static boolean isAir(BlockPos pos) {
      return MinecraftAccessor.mc.player != null && MinecraftAccessor.mc.world != null
         ? MinecraftAccessor.mc.world.getBlockState(pos).isAir()
         : false;
   }
}
