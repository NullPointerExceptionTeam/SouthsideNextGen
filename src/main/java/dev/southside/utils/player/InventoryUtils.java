package dev.southside.utils.player;

import dev.southside.module.impl.movement.NoSlowTest;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.item.ItemUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_10191;
import net.minecraft.class_10394;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
import net.minecraft.class_1744;
import net.minecraft.class_1747;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1779;
import net.minecraft.class_1781;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1809;
import net.minecraft.class_1810;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_1840;
import net.minecraft.class_1843;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2484;
import net.minecraft.class_2680;
import net.minecraft.class_2682;
import net.minecraft.class_310;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import net.minecraft.class_9334;

public class InventoryUtils {
   private static final class_310 mc = class_310.method_1551();

   public static boolean shouldDisableFeatures() {
      return NoSlowTest.shouldEnable ? true : getAllItems().stream().anyMatch(item -> {
         if (item.method_7960()) {
            return false;
         } else {
            String string = item.method_7964().getString();
            return string.contains("长按点击") || string.contains("点击使用") || string.contains("离开游戏") || string.contains("选择一个队伍") || string.contains("再来一局");
         }
      });
   }

   public static boolean isGoldenHead(class_1799 e) {
      if (e.method_7960()) {
         return false;
      } else {
         if (e.method_7909() instanceof class_1747) {
            class_1747 item = (class_1747)e.method_7909();
            if (item.method_7711() instanceof class_2484) {
               return true;
            }
         }

         return false;
      }
   }

   public static int findItem(int startSlot, int endSlot, class_1792 item) {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = startSlot; i < endSlot; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_7909() == item) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean isSharpnessAxe(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else if (!(stack.method_7909() instanceof class_1743)) {
         return false;
      } else {
         int itemEnchantmentLevel = EnchantmentUtils.getLevel(class_1893.field_9118, stack);
         return itemEnchantmentLevel >= 8 && itemEnchantmentLevel < 50;
      }
   }

   public static boolean isGodAxe(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else {
         String displayName = stack.method_7964().getString();
         if (displayName.contains("一刀")) {
            return true;
         } else {
            return stack.method_57353() != null && stack.method_57353().toString().contains("一刀")
               ? true
               : stack.method_7909() == class_1802.field_8825 && EnchantmentUtils.getLevel(class_1893.field_9118, stack) > 100;
         }
      }
   }

   public static boolean isEnchantedGApple(class_1799 stack) {
      return stack.method_7960() ? false : stack.method_7909() == class_1802.field_8367;
   }

   public static boolean isEndCrystal(class_1799 stack) {
      return stack.method_7960() ? false : stack.method_7909() == class_1802.field_8301;
   }

   public static boolean isKBBall(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else {
         return stack.method_7909() != class_1802.field_8777 ? false : !stack.method_57826(class_9334.field_49631);
      }
   }

   public static boolean isKBStick(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else {
         return stack.method_7909() != class_1802.field_8600 ? false : EnchantmentUtils.getLevel(class_1893.field_9121, stack) > 1;
      }
   }

   public static int findEmptyInventory() {
      for (int i = 9; i < mc.field_1724.method_31548().field_7547.size(); i++) {
         if (((class_1799)mc.field_1724.method_31548().field_7547.get(i)).method_7960()) {
            return i;
         }
      }

      return -1;
   }

   public static int findEmptySlot() {
      for (int i = 0; i < 9; i++) {
         if (((class_1799)mc.field_1724.method_31548().field_7547.get(i)).method_7960()) {
            return i;
         }
      }

      return -1;
   }

   public static int getPunchLevel(class_1799 stack) {
      return EnchantmentUtils.getLevel(class_1893.field_9116, stack);
   }

   public static int getPowerLevel(class_1799 stack) {
      return EnchantmentUtils.getLevel(class_1893.field_9103, stack);
   }

   public static List<class_1799> getAllItems() {
      ArrayList<class_1799> list = new ArrayList<>(40);
      list.addAll(mc.field_1724.method_31548().field_7547);
      list.addAll(mc.field_1724.method_31548().field_7548);
      return list;
   }

   public static boolean isInvalidBlock(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         if (stack.method_7909() instanceof class_1747 blockItem) {
            class_2680 state = blockItem.method_7711().method_9564();
            class_638 world = class_310.method_1551().field_1687;
            class_1922 view = (class_1922)(world != null ? world : class_2682.field_12294);
            return !state.method_26234(view, class_2338.field_10980) || Scaffold.invalidBlocks.contains(blockItem.method_7711());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static float getBestArmorScore(class_1304 slot) {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1738 && ItemUtils.getEquippableSlotType(item) == slot)
         .map(InventoryUtils::getProtection)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static float getCurrentArmorScore(class_1304 slot) {
      if (slot == class_1304.field_6169) {
         return getProtection((class_1799)mc.field_1724.method_31548().field_7548.get(3));
      } else if (slot == class_1304.field_6174) {
         return getProtection((class_1799)mc.field_1724.method_31548().field_7548.get(2));
      } else if (slot == class_1304.field_6172) {
         return getProtection((class_1799)mc.field_1724.method_31548().field_7548.get(1));
      } else {
         return slot == class_1304.field_6166 ? getProtection((class_1799)mc.field_1724.method_31548().field_7548.get(0)) : 0.0F;
      }
   }

   public static float getBestSwordDamage() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1829)
         .map(InventoryUtils::getSwordDamage)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestSword() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1829)
         .max(Comparator.comparingInt(s -> (int)(getSwordDamage(s) * 100.0F)))
         .orElse(null);
   }

   public static int getItemStackSlot(class_1799 stack) {
      if (stack == null) {
         return -1;
      } else {
         for (int i = 0; i < mc.field_1724.method_31548().field_7547.size(); i++) {
            if (mc.field_1724.method_31548().field_7547.get(i) == stack) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean isItemValid(class_1799 s) {
      if (!s.method_7960()) {
         if (s.method_7909() instanceof class_1809) {
            return false;
         }

         String string = s.method_7964().getString();
         if (string.contains("Click")) {
            return false;
         }

         if (string.contains("Right")) {
            return false;
         }

         if (string.contains("点击")) {
            return false;
         }

         if (string.contains("Teleport")) {
            return false;
         }

         if (string.contains("使用")) {
            return false;
         }

         if (string.contains("传送")) {
            return false;
         }

         if (string.contains("再来")) {
            return false;
         }
      }

      return true;
   }

   public static int getItemSlot(class_1792 item) {
      for (int i = 0; i < mc.field_1724.method_31548().field_7547.size(); i++) {
         class_1799 itemStack = (class_1799)mc.field_1724.method_31548().field_7547.get(i);
         if (itemStack.method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   public static class_1799 getBestProjectile() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.method_7960() && (item.method_7909() == class_1802.field_8803 || item.method_7909() == class_1802.field_8543) && isItemValid(item)
         )
         .max(Comparator.comparingInt(class_1799::method_7947))
         .orElse(null);
   }

   public static class_1799 getFishingRod() {
      return getAllItems().stream().filter(item -> !item.method_7960() && item.method_7909() instanceof class_1787 && isItemValid(item)).findAny().orElse(null);
   }

   public static int getBlockCountInInventory() {
      return getAllItems()
         .stream()
         .filter(
            stack -> !stack.method_7960()
               && stack.method_7909() instanceof class_1747
               && !stack.method_7909().equals(class_1802.field_8626)
               && !isInvalidBlock(stack)
               && isItemValid(stack)
         )
         .mapToInt(class_1799::method_7947)
         .sum();
   }

   public static class_1799 getWorstProjectile() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && (item.method_7909() == class_1802.field_8803 || item.method_7909() == class_1802.field_8543))
         .min(Comparator.comparingInt(class_1799::method_7947))
         .orElse(null);
   }

   public static class_1799 getWorstArrow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1744 && isItemValid(item))
         .min(Comparator.comparingInt(class_1799::method_7947))
         .orElse(null);
   }

   public static class_1799 getWorstBlock() {
      return getAllItems()
         .stream()
         .filter(stack -> !stack.method_7960() && stack.method_7909() instanceof class_1747 && !isInvalidBlock(stack) && isItemValid(stack))
         .min(Comparator.comparingInt(class_1799::method_7947))
         .orElse(null);
   }

   public static class_1799 getBestBlock() {
      return getAllItems()
         .stream()
         .filter(stack -> !stack.method_7960() && stack.method_7909() instanceof class_1747 && !isInvalidBlock(stack) && isItemValid(stack))
         .max(Comparator.comparingInt(class_1799::method_7947))
         .orElse(null);
   }

   public static float getBestPickaxeScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1810 && isItemValid(item))
         .map(InventoryUtils::getToolScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestPickaxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1810 && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getToolScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestAxeScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1743 && !isSharpnessAxe(item) && isItemValid(item))
         .map(InventoryUtils::getToolScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestAxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1743 && !isSharpnessAxe(item) && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getToolScore(s) * 100.0F)))
         .orElse(null);
   }

   public static class_1799 getBestShapeAxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1743 && isSharpnessAxe(item) && isItemValid(item) && !isGodAxe(item))
         .max(Comparator.comparingInt(s -> (int)(getAxeDamage(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestShovelScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1821 && isItemValid(item))
         .map(InventoryUtils::getToolScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestShovel() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1821 && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getToolScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestCrossbowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1764 && isItemValid(item))
         .map(InventoryUtils::getCrossbowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestCrossbow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1764 && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getCrossbowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestPunchBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1753 && isItemValid(item))
         .map(InventoryUtils::getPunchBowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestPunchBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1753 && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getPunchBowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestPowerBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1753 && isItemValid(item))
         .map(InventoryUtils::getPowerBowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestPowerBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1753 && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getPowerBowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestFlameBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1753 && isItemValid(item))
         .map(InventoryUtils::getFlameBowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static class_1799 getBestFlameBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7960() && item.method_7909() instanceof class_1753 && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getFlameBowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static boolean isPunchBow(class_1799 stack) {
      return getPunchBowScore(stack) > 10.0F && isItemValid(stack);
   }

   public static boolean isPowerBow(class_1799 stack) {
      return getPowerBowScore(stack) > 10.0F && isItemValid(stack);
   }

   public static boolean isFlameBow(class_1799 stack) {
      return getFlameBowScore(stack) > 10.0F && isItemValid(stack);
   }

   public static boolean hasItem(class_1792 checkItem) {
      return getAllItems().stream().anyMatch(item -> !item.method_7960() && item.method_7909() == checkItem);
   }

   public static int getItemCount(class_1792 checkItem) {
      return getAllItems().stream().filter(item -> !item.method_7960() && item.method_7909() == checkItem).mapToInt(class_1799::method_7947).sum();
   }

   public static float getPunchBowScore(class_1799 stack) {
      if (stack == null) {
         return 0.0F;
      } else if (stack.method_7960()) {
         return 0.0F;
      } else if (stack.method_7909() instanceof class_1753) {
         float valence = 10.0F;
         valence += EnchantmentUtils.getLevel(class_1893.field_9116, stack);
         valence += EnchantmentUtils.getLevel(class_1893.field_9125, stack);
         valence += EnchantmentUtils.getLevel(class_1893.field_9126, stack);
         valence += EnchantmentUtils.getLevel(class_1893.field_9103, stack) / 10.0F;
         return valence + (float)stack.method_7919() / stack.method_7936();
      } else {
         return 0.0F;
      }
   }

   public static float getPowerBowScore(class_1799 stack) {
      if (stack == null) {
         return 0.0F;
      } else if (stack.method_7960()) {
         return 0.0F;
      } else if (stack.method_7909() instanceof class_1753) {
         float valence = 10.0F;
         valence += EnchantmentUtils.getLevel(class_1893.field_9116, stack) / 10.0F;
         valence += EnchantmentUtils.getLevel(class_1893.field_9125, stack);
         valence += EnchantmentUtils.getLevel(class_1893.field_9126, stack);
         valence += EnchantmentUtils.getLevel(class_1893.field_9103, stack);
         return valence + (float)stack.method_7919() / stack.method_7936();
      } else {
         return 0.0F;
      }
   }

   public static float getFlameBowScore(class_1799 stack) {
      if (stack == null) {
         return 0.0F;
      } else if (stack.method_7960()) {
         return 0.0F;
      } else if (stack.method_7909() instanceof class_1753) {
         float valence = 10.0F;
         valence += EnchantmentUtils.getLevel(class_1893.field_9126, stack) * 2.0F;
         valence += EnchantmentUtils.getLevel(class_1893.field_9125, stack);
         valence += EnchantmentUtils.getLevel(class_1893.field_9103, stack) / 10.0F;
         valence += EnchantmentUtils.getLevel(class_1893.field_9116, stack) / 10.0F;
         return valence + (float)stack.method_7919() / stack.method_7936();
      } else {
         return 0.0F;
      }
   }

   public static float getToolScore(class_1799 stack) {
      float valence = 0.0F;
      if (stack == null) {
         return 0.0F;
      } else if (stack.method_7960()) {
         return 0.0F;
      } else if (isGodItem(stack)) {
         return 0.0F;
      } else if (isSharpnessAxe(stack)) {
         return 0.0F;
      } else {
         if (stack.method_7909() instanceof class_1810) {
            valence += stack.method_7924(class_2246.field_10340.method_9564());
         } else if (stack.method_7909() instanceof class_1743) {
            valence += stack.method_7924(class_2246.field_10431.method_9564());
         } else {
            if (!(stack.method_7909() instanceof class_1821)) {
               return 0.0F;
            }

            valence += stack.method_7924(class_2246.field_10566.method_9564());
         }

         int efficiency = EnchantmentUtils.getLevel(class_1893.field_9131, stack);
         if (efficiency > 0) {
            valence += efficiency * 0.0075F;
         }

         return valence;
      }
   }

   public static float getAxeDamage(class_1799 stack) {
      float valence = 0.0F;
      if (stack == null) {
         return 0.0F;
      } else if (stack.method_7960()) {
         return 0.0F;
      } else {
         if (stack.method_7909() instanceof class_1743 && isSharpnessAxe(stack)) {
            valence += (float)ItemUtils.handAttributeValueWithStack(stack, class_5134.field_23721, class_1268.field_5808);
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(class_1893.field_9118, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.25F * itemEnchantmentLevel;
         }

         return valence;
      }
   }

   public static float getSwordDamage(class_1799 stack) {
      float valence = 0.0F;
      if (stack == null) {
         return 0.0F;
      } else if (stack.method_7960()) {
         return 0.0F;
      } else {
         if (stack.method_7909() instanceof class_1829) {
            valence += (float)ItemUtils.handAttributeValueWithStack(stack, class_5134.field_23721, class_1268.field_5808);
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(class_1893.field_9118, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.25F * itemEnchantmentLevel;
         }

         return valence;
      }
   }

   public static float getProtection(class_1799 itemStack) {
      int valence = 0;
      if (itemStack == null) {
         return 0.0F;
      } else if (itemStack.method_7960()) {
         return 0.0F;
      } else {
         if (itemStack.method_7909() instanceof class_1738) {
            class_5321<class_10394> assets = ItemUtils.getArmorAsset(itemStack);
            if (assets != null) {
               if (class_10191.field_54134.equals(assets)) {
                  valence += 100;
               } else if (class_10191.field_54135.equals(assets)) {
                  valence += 200;
               } else if (class_10191.field_54136.equals(assets)) {
                  valence += 400;
               } else if (class_10191.field_54137.equals(assets)) {
                  valence += 300;
               } else if (class_10191.field_54138.equals(assets)) {
                  valence += 500;
               } else if (class_10191.field_54140.equals(assets)) {
                  valence += 600;
               }
            }
         }

         valence += EnchantmentUtils.getLevel(class_1893.field_9111, itemStack);
         return valence;
      }
   }

   public static float getCrossbowScore(class_1799 stack) {
      return ContainerUtils.getCrossbowScore(stack);
   }

   public static boolean isGodItem(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else if (stack.method_7909() instanceof class_1743
         && stack.method_7909() == class_1802.field_8825
         && EnchantmentUtils.getLevel(class_1893.field_9118, stack) > 100) {
         return true;
      } else if (stack.method_7909() == class_1802.field_8777 && EnchantmentUtils.getLevel(class_1893.field_9121, stack) > 1) {
         return true;
      } else {
         return stack.method_7909() == class_1802.field_8288 ? true : stack.method_7909() == class_1802.field_8301;
      }
   }

   public static boolean isCommonItemUseful(class_1799 stack) {
      if (stack.method_7960()) {
         return true;
      } else {
         class_1792 item = stack.method_7909();
         if (item instanceof class_1843) {
            return false;
         } else if (item instanceof class_1840) {
            return false;
         } else if (item instanceof class_1779) {
            return false;
         } else if (item instanceof class_1781) {
            return false;
         } else if (item.equals(class_1802.field_8106) || item.equals(class_1802.field_8782) || item.equals(class_1802.field_8657)) {
            return false;
         } else {
            return item == class_1802.field_8317
                  || item == class_1802.field_8309
                  || item == class_1802.field_46250
                  || item == class_1802.field_46249
                  || item == class_1802.field_42711
               ? false
               : item != class_1802.field_8884;
         }
      }
   }
}
