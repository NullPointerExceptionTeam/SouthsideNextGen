package dev.southside.utils.types;

import dev.southside.MinecraftAccessor;
import net.minecraft.class_1268;
import net.minecraft.class_1799;

public record FindItemResult(int slot, int count) {
   public boolean found() {
      return this.slot != -1;
   }

   public class_1268 getHand() {
      return this.slot == 45 ? class_1268.field_5810 : null;
   }

   public boolean isMainHand() {
      return this.getHand() == class_1268.field_5808;
   }

   public boolean isOffhand() {
      return this.getHand() == class_1268.field_5810;
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

   public class_1799 stack() {
      return MinecraftAccessor.mc.field_1724.method_31548().method_5438(this.slot);
   }
}
