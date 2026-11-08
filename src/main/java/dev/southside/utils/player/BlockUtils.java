package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.misc.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2189;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2399;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2530;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3865;

public final class BlockUtils {
   public static boolean isSolidBlockUnder(double height) {
      for (double y = 0.0; y < height; y++) {
         class_2338 pos = class_2338.method_49637(
            MinecraftAccessor.mc.field_1724.method_23317(), MinecraftAccessor.mc.field_1724.method_23318() - y, MinecraftAccessor.mc.field_1724.method_23321()
         );
         class_2680 state = MinecraftAccessor.mc.field_1687.method_8320(pos);
         if (!state.method_26215()
            && !MinecraftAccessor.mc
               .field_1687
               .method_20812(
                  MinecraftAccessor.mc.field_1724,
                  new class_238(
                     pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 1, pos.method_10260() + 1
                  )
               )
               .iterator()
               .hasNext()) {
            return true;
         }
      }

      return false;
   }

   public static boolean isValidBlock(class_243 block) {
      return isValidBlock(
         MinecraftAccessor.mc.field_1687.method_8320(new class_2338((int)block.field_1352, (int)block.field_1351, (int)block.field_1350)).method_26204()
      );
   }

   public static boolean isValidBlock(class_2248 block) {
      return !(block instanceof class_2404)
         && !(block instanceof class_2189)
         && !(block instanceof class_2281)
         && !(block instanceof class_3865)
         && !(block instanceof class_2399)
         && !(block instanceof class_2530);
   }

   public static boolean isStandingSafely() {
      if (MinecraftAccessor.mc.field_1724 != null && MinecraftAccessor.mc.field_1687 != null) {
         class_243 playerPos = MinecraftAccessor.mc.field_1724.method_19538();
         class_2338 centralPointBehind = new class_2338(
            (int)Math.floor(playerPos.field_1352 + Math.sin(Math.toRadians(MinecraftAccessor.mc.field_1724.method_36454())) * 0.8),
            (int)Math.floor(playerPos.field_1351),
            (int)Math.floor(playerPos.field_1350 + -Math.cos(Math.toRadians(MinecraftAccessor.mc.field_1724.method_36454())) * 0.8)
         );
         if (MinecraftAccessor.mc.field_1687.method_8320(centralPointBehind).method_26204() == class_2246.field_10164) {
            return false;
         } else {
            class_2338 pos = centralPointBehind.method_10074();
            class_2680 state = MinecraftAccessor.mc.field_1687.method_8320(pos);
            if (state.method_26204() == class_2246.field_10164) {
               return false;
            } else {
               if (state.method_26215()) {
                  if (pos.method_10264() < MinecraftAccessor.mc.field_1687.method_31607()) {
                     return false;
                  }

                  if (MinecraftAccessor.mc.field_1687.method_8320(pos.method_10074()).method_26215()) {
                     return pos.method_10074().method_10264() >= MinecraftAccessor.mc.field_1687.method_31607()
                        && !MinecraftAccessor.mc.field_1687.method_8320(pos.method_10074().method_10074()).method_26215();
                  }
               }

               return true;
            }
         }
      } else {
         return true;
      }
   }

   public static List<Pair<class_2338, class_2248>> getBlocksInRadius(int radius) {
      ArrayList<Pair<class_2338, class_2248>> blocks = new ArrayList<>();
      if (class_310.method_1551().field_1687 != null && class_310.method_1551().field_1724 != null) {
         for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
               for (int z = -radius; z <= radius; z++) {
                  double x1 = class_310.method_1551().field_1724.method_23317();
                  double y1 = class_310.method_1551().field_1724.method_23318();
                  double z1 = class_310.method_1551().field_1724.method_23321();
                  class_2338 blockPos = new class_2338((int)(x + x1), (int)(y + y1), (int)(z + z1));
                  blocks.add(new Pair<>(blockPos, class_310.method_1551().field_1687.method_8320(blockPos).method_26204()));
               }
            }
         }

         return blocks;
      } else {
         return null;
      }
   }
}
