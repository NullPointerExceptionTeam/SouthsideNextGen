package dev.southside.utils;

import dev.southside.MinecraftAccessor;
import net.minecraft.class_1713;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;

public class InventoryUtil {
   public static void swap(int slot, int hSlot) {
      MinecraftAccessor.mc
         .field_1761
         .method_2906(MinecraftAccessor.mc.field_1724.field_7512.field_7763, slot, hSlot, class_1713.field_7794, MinecraftAccessor.mc.field_1724);
   }

   public static boolean isFullBlock(class_1799 stack) {
      return stack.method_7909() instanceof class_1747 itemBlock;
   }

   public static int findItem(int startSlot, int endSlot, class_1792 item) {
      for (int i = startSlot; i < endSlot; i++) {
         class_1799 stack = MinecraftAccessor.mc.field_1724.method_31548().method_5438(i);
         if (stack != null && stack.method_7909() == item) {
            return i;
         }
      }

      return -1;
   }
}
