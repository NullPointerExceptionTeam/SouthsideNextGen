package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.item.ItemUtils;
import dev.southside.utils.types.FindItemResult;
import java.util.function.Predicate;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributes;

public class InvUtils {
   private static final InvUtils.Action ACTION = new InvUtils.Action();

   public static FindItemResult find(Item... items) {
      return find(itemStack -> {
         for (Item item : items) {
            if (itemStack.getItem() == item) {
               return true;
            }
         }

         return false;
      });
   }

   public static boolean isInventoryFull(PlayerInventory inventory) {
      for (ItemStack stack : inventory.main) {
         if (stack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public static boolean isSharpAxe(ItemStack stack) {
      return stack.getItem() instanceof AxeItem && stack.hasEnchantments() ? EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack) >= 10 : false;
   }

   public static boolean isKnockBackSlimeball(ItemStack stack) {
      return stack.getItem() == Items.SLIME_BALL && EnchantmentUtils.getLevel(Enchantments.KNOCKBACK, stack) == 3;
   }

   public static boolean isBestBow(ScreenHandler handler, ItemStack itemStack) {
      double bestBowDmg = -1.0;
      ItemStack bestBow = ItemStack.EMPTY;

      for (int i = 9; i < 45; i++) {
         ItemStack stack = handler.getSlot(i).getStack();
         if (!stack.isEmpty() && stack.getItem() instanceof BowItem) {
            double damage = getBowDamage(stack);
            if (damage > bestBowDmg) {
               bestBow = stack;
               bestBowDmg = damage;
            }
         }
      }

      return itemStack.equals(bestBow) || getBowDamage(itemStack) > bestBowDmg;
   }

   public static boolean isBestCrossBow(ScreenHandler handler, ItemStack itemStack) {
      double bestCrossbowDmg = -1.0;
      ItemStack bestCrossbow = ItemStack.EMPTY;

      for (int i = 9; i < 45; i++) {
         ItemStack stack = handler.getSlot(i).getStack();
         if (!stack.isEmpty() && stack.getItem() instanceof CrossbowItem) {
            double damage = getCrossbowDamage(stack);
            if (damage > bestCrossbowDmg) {
               bestCrossbowDmg = damage;
               bestCrossbow = stack;
            }
         }
      }

      return itemStack.equals(bestCrossbow) || getCrossbowDamage(itemStack) > bestCrossbowDmg;
   }

   public static double getCrossbowDamage(ItemStack stack) {
      double damage = 0.0;
      if (stack.getItem() instanceof CrossbowItem && stack.hasEnchantments()) {
         damage += EnchantmentUtils.getLevel(Enchantments.POWER, stack);
      }

      return damage;
   }

   public static double getBowDamage(ItemStack stack) {
      double damage = 0.0;
      if (stack.getItem() instanceof BowItem && stack.hasEnchantments()) {
         damage += EnchantmentUtils.getLevel(Enchantments.POWER, stack);
      }

      return damage;
   }

   public static boolean isBestTool(ScreenHandler handler, ItemStack itemStack) {
      int type = getToolType(itemStack);
      InvUtils.Tool bestTool = new InvUtils.Tool(-1, -1.0, ItemStack.EMPTY);

      for (int i = 9; i < 45; i++) {
         ItemStack stack = handler.getSlot(i).getStack();
         if (!stack.isEmpty() && ItemUtils.instanceofToolItem(stack.getItem()) && type == getToolType(stack)) {
            double efficiency = getToolScore(stack);
            if (efficiency > getToolScore(bestTool.getItem())) {
               bestTool = new InvUtils.Tool(i, efficiency, stack);
            }
         }
      }

      return bestTool.getItem().equals(itemStack) || getToolScore(itemStack) > getToolScore(bestTool.getItem());
   }

   public static float getToolScore(ItemStack stack) {
      float score = 0.0F;
      Item item = stack.getItem();
      if (ItemUtils.instanceofToolItem(item)) {
         if (item instanceof PickaxeItem) {
            score = stack.getMiningSpeedMultiplier(Blocks.STONE.getDefaultState());
         } else {
            if (!(item instanceof AxeItem)) {
               return 1.0F;
            }

            score = stack.getMiningSpeedMultiplier(Blocks.DARK_OAK_LOG.getDefaultState());
         }

         score += EnchantmentUtils.getLevel(Enchantments.EFFICIENCY, stack) * 0.0075F;
         score += EnchantmentUtils.getLevel(Enchantments.EFFICIENCY, stack) / 100.0F;
         score += EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
      }

      return score;
   }

   public static int getToolType(ItemStack stack) {
      Item tool = stack.getItem();
      if (tool instanceof PickaxeItem) {
         return 0;
      } else {
         return tool instanceof AxeItem ? 1 : -1;
      }
   }

   public static InvUtils.Action click() {
      ACTION.type = SlotActionType.PICKUP;
      return ACTION;
   }

   public static boolean isBestSword(ScreenHandler c, ItemStack itemStack, boolean checkInv) {
      double currentDamage = getItemDamage(itemStack);
      double bestContainerDamage = 0.0;
      ItemStack bestContainerStack = ItemStack.EMPTY;

      for (int i = 9; i < 45; i++) {
         ItemStack stack = c.getSlot(i).getStack();
         if (!stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof TridentItem)) {
            double newDamage = getItemDamage(stack);
            if (newDamage > bestContainerDamage) {
               bestContainerDamage = newDamage;
               bestContainerStack = stack;
            }
         }
      }

      if (checkInv && MinecraftAccessor.mc.player != null) {
         double bestInventoryDamage = 0.0;

         for (int ix = 0; ix < 36; ix++) {
            ItemStack stack = MinecraftAccessor.mc.player.getInventory().getStack(ix);
            if (!stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof TridentItem)) {
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

   public static double getDamageReduction(ItemStack stack) {
      if (stack.getItem() instanceof ArmorItem armor) {
         double var9 = ItemUtils.armorAttributeValueWithStack(stack, EntityAttributes.ARMOR);
         double toughness = ItemUtils.armorAttributeValueWithStack(stack, EntityAttributes.ARMOR_TOUGHNESS);
         int protLevel = EnchantmentUtils.getLevel(Enchantments.PROTECTION, stack);
         double enchantBonus = protLevel * 0.04;
         return var9 + toughness * 0.5 + enchantBonus * 10.0;
      } else {
         return 0.0;
      }
   }

   public static boolean isBestArmor(ScreenHandler c, ItemStack itemStack, boolean checkInv) {
      ArmorItem itemArmor = (ArmorItem)itemStack.getItem();
      double currentReduction = getDamageReduction(itemStack);
      double bestEquippedReduction = 0.0;
      if (MinecraftAccessor.mc.player != null) {
         ItemStack equippedStack = MinecraftAccessor.mc.player.getEquippedStack(ItemUtils.getEquippableSlotType(itemStack));
         if (!equippedStack.isEmpty()
            && equippedStack.getItem() instanceof ArmorItem equippedArmor
            && ItemUtils.getEquippableSlotType(equippedStack) == ItemUtils.getEquippableSlotType(itemStack)) {
            bestEquippedReduction = getDamageReduction(equippedStack);
         }
      }

      double bestContainerReduction = 0.0;
      ItemStack bestContainerStack = ItemStack.EMPTY;

      for (int i = 5; i < 45; i++) {
         ItemStack stack = c.getSlot(i).getStack();
         if (!stack.isEmpty()
            && stack.getItem() instanceof ArmorItem stackArmor
            && ItemUtils.getEquippableSlotType(stack).getIndex() - 1 == ItemUtils.getEquippableSlotType(itemStack).getIndex() - 1) {
            double newReduction = getDamageReduction(stack);
            if (newReduction > bestContainerReduction) {
               bestContainerReduction = newReduction;
               bestContainerStack = stack;
            }
         }
      }

      if (checkInv && MinecraftAccessor.mc.player != null) {
         double bestInventoryReduction = 0.0;

         for (int ix = 0; ix < 36; ix++) {
            ItemStack stack = MinecraftAccessor.mc.player.getInventory().getStack(ix);
            if (!stack.isEmpty()
               && stack.getItem() instanceof ArmorItem stackArmorx
               && ItemUtils.getEquippableSlotType(stack).getIndex() - 1 == ItemUtils.getEquippableSlotType(itemStack).getIndex() - 1) {
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

   public static FindItemResult find(Predicate<ItemStack> isGood) {
      return MinecraftAccessor.mc.player == null ? new FindItemResult(0, 0) : find(isGood, 0, MinecraftAccessor.mc.player.getInventory().size());
   }

   public static FindItemResult find(Predicate<ItemStack> isGood, int start, int end) {
      if (MinecraftAccessor.mc.player == null) {
         return new FindItemResult(0, 0);
      } else {
         int slot = -1;
         int count = 0;

         for (int i = start; i <= end; i++) {
            ItemStack stack = MinecraftAccessor.mc.player.getInventory().getStack(i);
            if (isGood.test(stack)) {
               if (slot == -1) {
                  slot = i;
               }

               count += stack.getCount();
            }
         }

         return new FindItemResult(slot, count);
      }
   }

   public static double getItemDamage(ItemStack stack) {
      double damage = ItemUtils.handAttributeValueWithStack(stack, EntityAttributes.ATTACK_DAMAGE, Hand.MAIN_HAND);
      int sharpness = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
      if (sharpness > 0) {
         damage += 1.0 + (sharpness - 1) * 0.5;
      }

      return damage;
   }

   public static int findItemInInv(Item item) {
      if (MinecraftAccessor.mc.player == null) {
         return -1;
      } else {
         for (int slot = 9; slot < MinecraftAccessor.mc.player.getInventory().size(); slot++) {
            ItemStack stack = MinecraftAccessor.mc.player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
               return slot;
            }
         }

         return -1;
      }
   }

   public static class Action {
      private SlotActionType type = null;
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
         boolean hadEmptyCursor = MinecraftAccessor.mc.player.currentScreenHandler.getCursorStack().isEmpty();
         if (this.type == SlotActionType.SWAP) {
            this.data = this.from;
            this.from = this.to;
         }

         if (this.type != null && this.from != -1 && this.to != -1) {
            this.click(this.from);
            if (this.two) {
               this.click(this.to);
            }
         }

         SlotActionType preType = this.type;
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
            && preType == SlotActionType.PICKUP
            && preTwo
            && preFrom != -1
            && preTo != -1
            && !MinecraftAccessor.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            this.isRecursive = true;
            InvUtils.click().slotId(preFrom);
            this.isRecursive = false;
         }
      }

      private void click(int id) {
         MinecraftAccessor.mc
            .interactionManager
            .clickSlot(MinecraftAccessor.mc.player.currentScreenHandler.syncId, id, this.data, this.type, MinecraftAccessor.mc.player);
      }
   }

   private record Tool(int slot, double efficiency, ItemStack stack) {
      public ItemStack getItem() {
         return this.stack;
      }
   }
}
