package dev.southside.utils;

import dev.southside.MinecraftAccessor;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtil {
   public static void swap(int slot, int hSlot) {
      MinecraftAccessor.mc
         .interactionManager
         .clickSlot(MinecraftAccessor.mc.player.currentScreenHandler.syncId, slot, hSlot, SlotActionType.QUICK_MOVE, MinecraftAccessor.mc.player);
   }

   public static boolean isFullBlock(ItemStack stack) {
      return stack.getItem() instanceof BlockItem itemBlock;
   }

   public static int findItem(int startSlot, int endSlot, Item item) {
      for (int i = startSlot; i < endSlot; i++) {
         ItemStack stack = MinecraftAccessor.mc.player.getInventory().getStack(i);
         if (stack != null && stack.getItem() == item) {
            return i;
         }
      }

      return -1;
   }
}
