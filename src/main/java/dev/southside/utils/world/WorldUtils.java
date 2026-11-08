package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import net.minecraft.class_2338;
import net.minecraft.class_2586;

public class WorldUtils {
   public static Iterable<class_2586> blockEntities() {
      return BlockEntityIterator::new;
   }

   public static boolean isAir(class_2338 pos) {
      return MinecraftAccessor.mc.field_1724 != null && MinecraftAccessor.mc.field_1687 != null
         ? MinecraftAccessor.mc.field_1687.method_8320(pos).method_26215()
         : false;
   }
}
