package dev.southside.utils.types;

import dev.southside.MinecraftAccessor;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

public record FindItemResult(int slot, int count) {
   public boolean found() {
      return this.slot != -1;
   }

   public Hand getHand() {
      return this.slot == 45 ? Hand.OFF_HAND : null;
   }

   public boolean isMainHand() {
      return this.getHand() == Hand.MAIN_HAND;
   }

   public boolean isOffhand() {
      return this.getHand() == Hand.OFF_HAND;
   }

   public boolean isHotbar() {
      return this.slot >= 0 && this.slot <= 8;
   }

   public boolean isMain() {
      return this.slot >= 9 && this.slot <= 35;
   }

   public boolean isArmor() {
      return this.slot >= 36 && this.slot <= 39;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FindItemResult that = (FindItemResult)o;
         return this.slot == that.slot;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.slot;
   }

   public ItemStack stack() {
      return MinecraftAccessor.mc.player.getInventory().getStack(this.slot);
   }
}
