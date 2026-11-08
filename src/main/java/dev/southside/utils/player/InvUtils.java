package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.item.ItemUtils;
import dev.southside.utils.types.FindItemResult;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1810;
import net.minecraft.class_1829;
import net.minecraft.class_1835;
import net.minecraft.class_1893;
import net.minecraft.class_2246;
import net.minecraft.class_5134;

public class InvUtils {
   private static final InvUtils.Action ACTION = new InvUtils.Action();

   public static FindItemResult find(class_1792... items) {
      return find(itemStack -> {
         for (class_1792 item : items) {
            if (itemStack.method_7909() == item) {
               return true;
            }
         }

         return false;
      });
   }

   public static boolean isInventoryFull(class_1661 inventory) {
      for (class_1799 stack : inventory.field_7547) {
         if (stack.method_7960()) {
            return false;
         }
      }

      return true;
   }

   public static boolean isSharpAxe(class_1799 stack) {
      return stack.method_7909() instanceof class_1743 && stack.method_7942() ? EnchantmentUtils.getLevel(class_1893.field_9118, stack) >= 10 : false;
   }

   public static boolean isKnockBackSlimeball(class_1799 stack) {
      return stack.method_7909() == class_1802.field_8777 && EnchantmentUtils.getLevel(class_1893.field_9121, stack) == 3;
   }

   public static boolean isBestBow(class_1703 handler, class_1799 itemStack) {
      double bestBowDmg = -1.0;
      class_1799 bestBow = class_1799.field_8037;

      for (int i = 9; i < 45; i++) {
         class_1799 stack = handler.method_7611(i).method_7677();
         if (!stack.method_7960() && stack.method_7909() instanceof class_1753) {
            double damage = getBowDamage(stack);
            if (damage > bestBowDmg) {
               bestBow = stack;
               bestBowDmg = damage;
            }
         }
      }

      return itemStack.equals(bestBow) || getBowDamage(itemStack) > bestBowDmg;
   }

   public static boolean isBestCrossBow(class_1703 handler, class_1799 itemStack) {
      double bestCrossbowDmg = -1.0;
      class_1799 bestCrossbow = class_1799.field_8037;

      for (int i = 9; i < 45; i++) {
         class_1799 stack = handler.method_7611(i).method_7677();
         if (!stack.method_7960() && stack.method_7909() instanceof class_1764) {
            double damage = getCrossbowDamage(stack);
            if (damage > bestCrossbowDmg) {
               bestCrossbowDmg = damage;
               bestCrossbow = stack;
            }
         }
      }

      return itemStack.equals(bestCrossbow) || getCrossbowDamage(itemStack) > bestCrossbowDmg;
   }

   public static double getCrossbowDamage(class_1799 stack) {
      double damage = 0.0;
      if (stack.method_7909() instanceof class_1764 && stack.method_7942()) {
         damage += EnchantmentUtils.getLevel(class_1893.field_9103, stack);
      }

      return damage;
   }

   public static double getBowDamage(class_1799 stack) {
      double damage = 0.0;
      if (stack.method_7909() instanceof class_1753 && stack.method_7942()) {
         damage += EnchantmentUtils.getLevel(class_1893.field_9103, stack);
      }

      return damage;
   }

   public static boolean isBestTool(class_1703 handler, class_1799 itemStack) {
      int type = getToolType(itemStack);
      InvUtils.Tool bestTool = new InvUtils.Tool(-1, -1.0, class_1799.field_8037);

      for (int i = 9; i < 45; i++) {
         class_1799 stack = handler.method_7611(i).method_7677();
         if (!stack.method_7960() && ItemUtils.instanceofToolItem(stack.method_7909()) && type == getToolType(stack)) {
            double efficiency = getToolScore(stack);
            if (efficiency > getToolScore(bestTool.getItem())) {
               bestTool = new InvUtils.Tool(i, efficiency, stack);
            }
         }
      }

      return bestTool.getItem().equals(itemStack) || getToolScore(itemStack) > getToolScore(bestTool.getItem());
   }

   public static float getToolScore(class_1799 stack) {
      float score = 0.0F;
      class_1792 item = stack.method_7909();
      if (ItemUtils.instanceofToolItem(item)) {
         if (item instanceof class_1810) {
            score = stack.method_7924(class_2246.field_10340.method_9564());
         } else {
            if (!(item instanceof class_1743)) {
               return 1.0F;
            }

            score = stack.method_7924(class_2246.field_10010.method_9564());
         }

         score += EnchantmentUtils.getLevel(class_1893.field_9131, stack) * 0.0075F;
         score += EnchantmentUtils.getLevel(class_1893.field_9131, stack) / 100.0F;
         score += EnchantmentUtils.getLevel(class_1893.field_9118, stack);
      }

      return score;
   }

   public static int getToolType(class_1799 stack) {
      class_1792 tool = stack.method_7909();
      if (tool instanceof class_1810) {
         return 0;
      } else {
         return tool instanceof class_1743 ? 1 : -1;
      }
   }

   public static InvUtils.Action click() {
      ACTION.type = class_1713.field_7790;
      return ACTION;
   }

   public static boolean isBestSword(class_1703 c, class_1799 itemStack, boolean checkInv) {
      double currentDamage = getItemDamage(itemStack);
      double bestContainerDamage = 0.0;
      class_1799 bestContainerStack = class_1799.field_8037;

      for (int i = 9; i < 45; i++) {
         class_1799 stack = c.method_7611(i).method_7677();
         if (!stack.method_7960() && (stack.method_7909() instanceof class_1829 || stack.method_7909() instanceof class_1835)) {
            double newDamage = getItemDamage(stack);
            if (newDamage > bestContainerDamage) {
               bestContainerDamage = newDamage;
               bestContainerStack = stack;
            }
         }
      }

      if (checkInv && MinecraftAccessor.mc.field_1724 != null) {
         double bestInventoryDamage = 0.0;

         for (int ix = 0; ix < 36; ix++) {
            class_1799 stack = MinecraftAccessor.mc.field_1724.method_31548().method_5438(ix);
            if (!stack.method_7960() && (stack.method_7909() instanceof class_1829 || stack.method_7909() instanceof class_1835)) {
               double newDamage = getItemDamage(stack);
               if (newDamage > bestInventoryDamage) {
                  bestInventoryDamage = newDamage;
               }
            }
         }

         return currentDamage > bestInventoryDamage;
      } else {
         return bestContainerStack.equals(itemStack) || currentDamage > bestContainerDamage;
      }
   }

   public static double getDamageReduction(class_1799 stack) {
      if (stack.method_7909() instanceof class_1738 armor) {
         double var9 = ItemUtils.armorAttributeValueWithStack(stack, class_5134.field_23724);
         double toughness = ItemUtils.armorAttributeValueWithStack(stack, class_5134.field_23725);
         int protLevel = EnchantmentUtils.getLevel(class_1893.field_9111, stack);
         double enchantBonus = protLevel * 0.04;
         return var9 + toughness * 0.5 + enchantBonus * 10.0;
      } else {
         return 0.0;
      }
   }

   public static boolean isBestArmor(class_1703 c, class_1799 itemStack, boolean checkInv) {
      class_1738 itemArmor = (class_1738)itemStack.method_7909();
      double currentReduction = getDamageReduction(itemStack);
      double bestEquippedReduction = 0.0;
      if (MinecraftAccessor.mc.field_1724 != null) {
         class_1799 equippedStack = MinecraftAccessor.mc.field_1724.method_6118(ItemUtils.getEquippableSlotType(itemStack));
         if (!equippedStack.method_7960()
            && equippedStack.method_7909() instanceof class_1738 equippedArmor
            && ItemUtils.getEquippableSlotType(equippedStack) == ItemUtils.getEquippableSlotType(itemStack)) {
            bestEquippedReduction = getDamageReduction(equippedStack);
         }
      }

      double bestContainerReduction = 0.0;
      class_1799 bestContainerStack = class_1799.field_8037;

      for (int i = 5; i < 45; i++) {
         class_1799 stack = c.method_7611(i).method_7677();
         if (!stack.method_7960()
            && stack.method_7909() instanceof class_1738 stackArmor
            && ItemUtils.getEquippableSlotType(stack).method_63622() - 1 == ItemUtils.getEquippableSlotType(itemStack).method_63622() - 1) {
            double newReduction = getDamageReduction(stack);
            if (newReduction > bestContainerReduction) {
               bestContainerReduction = newReduction;
               bestContainerStack = stack;
            }
         }
      }

      if (checkInv && MinecraftAccessor.mc.field_1724 != null) {
         double bestInventoryReduction = 0.0;

         for (int ix = 0; ix < 36; ix++) {
            class_1799 stack = MinecraftAccessor.mc.field_1724.method_31548().method_5438(ix);
            if (!stack.method_7960()
               && stack.method_7909() instanceof class_1738 stackArmorx
               && ItemUtils.getEquippableSlotType(stack).method_63622() - 1 == ItemUtils.getEquippableSlotType(itemStack).method_63622() - 1) {
               double newReduction = getDamageReduction(stack);
               if (newReduction > bestInventoryReduction) {
                  bestInventoryReduction = newReduction;
               }
            }
         }

         return currentReduction > Math.max(bestInventoryReduction, bestEquippedReduction);
      } else {
         return bestContainerStack.equals(itemStack) || currentReduction > Math.max(bestContainerReduction, bestEquippedReduction);
      }
   }

   public static FindItemResult find(Predicate<class_1799> isGood) {
      return MinecraftAccessor.mc.field_1724 == null ? new FindItemResult(0, 0) : find(isGood, 0, MinecraftAccessor.mc.field_1724.method_31548().method_5439());
   }

   public static FindItemResult find(Predicate<class_1799> isGood, int start, int end) {
      if (MinecraftAccessor.mc.field_1724 == null) {
         return new FindItemResult(0, 0);
      } else {
         int slot = -1;
         int count = 0;

         for (int i = start; i <= end; i++) {
            class_1799 stack = MinecraftAccessor.mc.field_1724.method_31548().method_5438(i);
            if (isGood.test(stack)) {
               if (slot == -1) {
                  slot = i;
               }

               count += stack.method_7947();
            }
         }

         return new FindItemResult(slot, count);
      }
   }

   public static double getItemDamage(class_1799 stack) {
      double damage = ItemUtils.handAttributeValueWithStack(stack, class_5134.field_23721, class_1268.field_5808);
      int sharpness = EnchantmentUtils.getLevel(class_1893.field_9118, stack);
      if (sharpness > 0) {
         damage += 1.0 + (sharpness - 1) * 0.5;
      }

      return damage;
   }

   public static int findItemInInv(class_1792 item) {
      if (MinecraftAccessor.mc.field_1724 == null) {
         return -1;
      } else {
         for (int slot = 9; slot < MinecraftAccessor.mc.field_1724.method_31548().method_5439(); slot++) {
            class_1799 stack = MinecraftAccessor.mc.field_1724.method_31548().method_5438(slot);
            if (!stack.method_7960() && stack.method_7909() == item) {
               return slot;
            }
         }

         return -1;
      }
   }

   public static class Action {
      private class_1713 type = null;
      private boolean two = false;
      private int from = -1;
      private int to = -1;
      private int data = 0;
      private boolean isRecursive = false;

      private Action() {
      }

      public InvUtils.Action fromId(int id) {
         this.from = id;
         return this;
      }

      public InvUtils.Action from(int index) {
         return this.fromId(SlotUtils.indexToId(index));
      }

      public InvUtils.Action fromHotbar(int i) {
         return this.from(0 + i);
      }

      public InvUtils.Action fromOffhand() {
         return this.from(45);
      }

      public InvUtils.Action fromMain(int i) {
         return this.from(9 + i);
      }

      public InvUtils.Action fromArmor(int i) {
         return this.from(36 + (3 - i));
      }

      public void toId(int id) {
         this.to = id;
         this.run();
      }

      public void to(int index) {
         this.toId(SlotUtils.indexToId(index));
      }

      public void toHotbar(int i) {
         this.to(0 + i);
      }

      public void toOffhand() {
         this.to(45);
      }

      public void toMain(int i) {
         this.to(9 + i);
      }

      public void toArmor(int i) {
         this.to(36 + (3 - i));
      }

      public void slotId(int id) {
         this.from = this.to = id;
         this.run();
      }

      public void slot(int index) {
         this.slotId(SlotUtils.indexToId(index));
      }

      public void slotHotbar(int i) {
         this.slot(0 + i);
      }

      public void slotOffhand() {
         this.slot(45);
      }

      public void slotMain(int i) {
         this.slot(9 + i);
      }

      public void slotArmor(int i) {
         this.slot(36 + (3 - i));
      }

      private void run() {
         boolean hadEmptyCursor = MinecraftAccessor.mc.field_1724.field_7512.method_34255().method_7960();
         if (this.type == class_1713.field_7791) {
            this.data = this.from;
            this.from = this.to;
         }

         if (this.type != null && this.from != -1 && this.to != -1) {
            this.click(this.from);
            if (this.two) {
               this.click(this.to);
            }
         }

         class_1713 preType = this.type;
         boolean preTwo = this.two;
         int preFrom = this.from;
         int preTo = this.to;
         this.type = null;
         this.two = false;
         this.from = -1;
         this.to = -1;
         this.data = 0;
         if (!this.isRecursive
            && hadEmptyCursor
            && preType == class_1713.field_7790
            && preTwo
            && preFrom != -1
            && preTo != -1
            && !MinecraftAccessor.mc.field_1724.field_7512.method_34255().method_7960()) {
            this.isRecursive = true;
            InvUtils.click().slotId(preFrom);
            this.isRecursive = false;
         }
      }

      private void click(int id) {
         MinecraftAccessor.mc
            .field_1761
            .method_2906(MinecraftAccessor.mc.field_1724.field_7512.field_7763, id, this.data, this.type, MinecraftAccessor.mc.field_1724);
      }
   }

   private record Tool(int slot, double efficiency, class_1799 stack) {
      public class_1799 getItem() {
         return this.stack;
      }
   }
}
