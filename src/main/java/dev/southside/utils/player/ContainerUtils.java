package dev.southside.utils.player;

import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.item.ItemUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_10191;
import net.minecraft.class_10394;
import net.minecraft.class_1304;
import net.minecraft.class_1735;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
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
import net.minecraft.class_2246;
import net.minecraft.class_310;
import net.minecraft.class_5321;

public class ContainerUtils {
   private static final class_310 mc = class_310.method_1551();

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
         } else if (!stack.method_57353().method_57837() && stack.method_57353().toString().contains("一刀")) {
            return true;
         } else {
            return stack.method_7909() != class_1802.field_8825 ? false : EnchantmentUtils.getLevel(class_1893.field_9118, stack) > 100;
         }
      }
   }

   public static List<class_1735> getAllItems() {
      ArrayList<class_1735> list = new ArrayList<>(40);

      for (class_1735 slot : mc.field_1724.field_7512.field_7761) {
         list.add(slot);
      }

      return list;
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
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1829)
         .sorted(Comparator.comparingDouble(s -> getSwordDamage(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> getSwordDamage(s.method_7677()))
         .findFirst()
         .orElse(0.0F);
   }

   public static class_1799 getBestSword() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1829)
         .sorted(Comparator.comparingInt(s -> (int)(getSwordDamage(s.method_7677()) * 100.0F)))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .<class_1799>map(class_1735::method_7677)
         .findFirst()
         .orElse(null);
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
            item -> !item.method_7677().method_7960()
               && (item.method_7677().method_7909() == class_1802.field_8803 || item.method_7677().method_7909() == class_1802.field_8543)
               && isItemValid(item.method_7677())
         )
         .sorted(Comparator.comparingDouble(s -> -s.method_7677().method_7947()))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .<class_1799>map(class_1735::method_7677)
         .findFirst()
         .orElse(null);
   }

   public static class_1799 getFishingRod() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1787 && isItemValid(item.method_7677()))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .<class_1799>map(class_1735::method_7677)
         .findFirst()
         .orElse(null);
   }

   public static class_1799 getBestBlock() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.method_7677().method_7960()
               && item.method_7677().method_7909() instanceof class_1747 b
               && !Scaffold.invalidBlocks.contains(b.method_7711())
               && isItemValid(item.method_7677())
         )
         .sorted(Comparator.comparingDouble(s -> -s.method_7677().method_7947()))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .<class_1799>map(class_1735::method_7677)
         .findFirst()
         .orElse(null);
   }

   public static float getBestPickaxeScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1810 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> getToolScore(s.method_7677()))
         .findFirst()
         .orElse(0.0F);
   }

   public static class_1799 getBestPickaxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1810 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .<class_1799>map(class_1735::method_7677)
         .findFirst()
         .orElse(null);
   }

   public static float getBestAxeScore() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.method_7677().method_7960()
               && item.method_7677().method_7909() instanceof class_1743
               && !isSharpnessAxe(item.method_7677())
               && isItemValid(item.method_7677())
         )
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> getToolScore(s.method_7677()))
         .findFirst()
         .orElse(0.0F);
   }

   public static class_1799 getBestAxe() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.method_7677().method_7960()
               && item.method_7677().method_7909() instanceof class_1743
               && !isSharpnessAxe(item.method_7677())
               && isItemValid(item.method_7677())
         )
         .sorted(Comparator.comparingInt(s -> -((int)(getToolScore(s.method_7677()) * 100.0F))))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .<class_1799>map(class_1735::method_7677)
         .findFirst()
         .orElse(null);
   }

   public static class_1799 getBestShapeAxe() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.method_7677().method_7960()
               && item.method_7677().method_7909() instanceof class_1743
               && isSharpnessAxe(item.method_7677())
               && isItemValid(item.method_7677())
               && !isGodAxe(item.method_7677())
         )
         .sorted(Comparator.comparingInt(s -> -((int)(getAxeDamage(s.method_7677()) * 100.0F))))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> s.method_7677())
         .findFirst()
         .orElse(null);
   }

   public static float getBestShovelScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1821 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> getToolScore(s.method_7677()))
         .findFirst()
         .orElse(0.0F);
   }

   public static class_1799 getBestShovel() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1821 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingInt(s -> -((int)(getAxeDamage(s.method_7677()) * 100.0F))))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> s.method_7677())
         .findFirst()
         .orElse(null);
   }

   public static float getBestCrossbowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1764 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingDouble(s -> -getCrossbowScore(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> getCrossbowScore(s.method_7677()))
         .findFirst()
         .orElse(0.0F);
   }

   public static class_1799 getBestCrossbow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1764 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingInt(s -> -((int)(getCrossbowScore(s.method_7677()) * 100.0F))))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> s.method_7677())
         .findFirst()
         .orElse(null);
   }

   public static float getBestPunchBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1753 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingDouble(s -> -getPunchBowScore(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> getPunchBowScore(s.method_7677()))
         .findFirst()
         .orElse(0.0F);
   }

   public static class_1799 getBestPunchBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1753 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingInt(s -> -((int)(getPunchBowScore(s.method_7677()) * 100.0F))))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> s.method_7677())
         .findFirst()
         .orElse(null);
   }

   public static float getBestPowerBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1753 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingDouble(s -> -getPowerBowScore(s.method_7677())))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> getPowerBowScore(s.method_7677()))
         .findFirst()
         .orElse(0.0F);
   }

   public static class_1799 getBestPowerBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() instanceof class_1753 && isItemValid(item.method_7677()))
         .sorted(Comparator.comparingInt(s -> -((int)(getPowerBowScore(s.method_7677()) * 100.0F))))
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .map(s -> s.method_7677())
         .findFirst()
         .orElse(null);
   }

   public static int getItemCount(class_1792 checkItem) {
      return getAllItems()
         .stream()
         .filter(s -> s.field_7871 != mc.field_1724.method_31548())
         .filter(item -> !item.method_7677().method_7960() && item.method_7677().method_7909() == checkItem)
         .mapToInt(s -> s.method_7677().method_7947())
         .sum();
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
            class_1743 axe = (class_1743)stack.method_7909();
            if (axe == class_1802.field_8406) {
               valence += 4.0F;
            } else if (axe == class_1802.field_8062) {
               valence += 5.0F;
            } else if (axe == class_1802.field_8475) {
               valence += 6.0F;
            } else if (axe == class_1802.field_8825) {
               valence += 4.0F;
            } else if (axe == class_1802.field_8556) {
               valence += 7.0F;
            }
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(class_1893.field_9118, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.0F + (itemEnchantmentLevel - 1) * 0.5F;
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
            valence += stack.method_7919() + 1.0F;
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(class_1893.field_9118, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.0F + (itemEnchantmentLevel - 1) * 0.5F;
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
         if (itemStack.method_7909() instanceof class_1738 armor) {
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
      int valence = 0;
      if (stack == null) {
         return 0.0F;
      } else if (stack.method_7960()) {
         return 0.0F;
      } else {
         if (stack.method_7909() instanceof class_1764) {
            valence += EnchantmentUtils.getLevel(class_1893.field_9098, stack);
            valence += EnchantmentUtils.getLevel(class_1893.field_9108, stack);
            valence += EnchantmentUtils.getLevel(class_1893.field_9132, stack);
         }

         return valence;
      }
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
         if (item instanceof class_1747 block) {
            if (block.method_7711() == class_2246.field_10485) {
               return false;
            }

            if (block.method_7711() == class_2246.field_10343) {
               return false;
            }
         } else {
            if (item instanceof class_1843) {
               return false;
            }

            if (item instanceof class_1840) {
               return false;
            }

            if (item instanceof class_1779) {
               return false;
            }

            if (item instanceof class_1781) {
               return false;
            }

            if (item == class_1802.field_8317 || item == class_1802.field_8309 || item == class_1802.field_46250 || item == class_1802.field_46249) {
               return false;
            }

            if (item == class_1802.field_8884) {
               return false;
            }
         }

         return true;
      }
   }
}
