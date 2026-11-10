package dev.southside.utils.player;

import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.item.ItemUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;

public class ContainerUtils {
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public static boolean isSharpnessAxe(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (!(stack.getItem() instanceof AxeItem)) {
         return false;
      } else {
         int itemEnchantmentLevel = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
         return itemEnchantmentLevel >= 8 && itemEnchantmentLevel < 50;
      }
   }

   public static boolean isGodAxe(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         String displayName = stack.getName().getString();
         if (displayName.contains("一刀")) {
            return true;
         } else if (!stack.getComponents().isEmpty() && stack.getComponents().toString().contains("一刀")) {
            return true;
         } else {
            return stack.getItem() != Items.GOLDEN_AXE ? false : EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack) > 100;
         }
      }
   }

   public static List<Slot> getAllItems() {
      ArrayList<Slot> list = new ArrayList<>(40);

      for (Slot slot : mc.player.currentScreenHandler.slots) {
         list.add(slot);
      }

      return list;
   }

   public static float getCurrentArmorScore(EquipmentSlot slot) {
      if (slot == EquipmentSlot.HEAD) {
         return getProtection((ItemStack)mc.player.getInventory().armor.get(3));
      } else if (slot == EquipmentSlot.CHEST) {
         return getProtection((ItemStack)mc.player.getInventory().armor.get(2));
      } else if (slot == EquipmentSlot.LEGS) {
         return getProtection((ItemStack)mc.player.getInventory().armor.get(1));
      } else {
         return slot == EquipmentSlot.FEET ? getProtection((ItemStack)mc.player.getInventory().armor.get(0)) : 0.0F;
      }
   }

   public static float getBestSwordDamage() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof SwordItem)
         .sorted(Comparator.comparingDouble(s -> getSwordDamage(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> getSwordDamage(s.getStack()))
         .findFirst()
         .orElse(0.0F);
   }

   public static ItemStack getBestSword() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof SwordItem)
         .sorted(Comparator.comparingInt(s -> (int)(getSwordDamage(s.getStack()) * 100.0F)))
         .filter(s -> s.inventory != mc.player.getInventory())
         .<ItemStack>map(Slot::getStack)
         .findFirst()
         .orElse(null);
   }

   public static boolean isItemValid(ItemStack s) {
      if (!s.isEmpty()) {
         if (s.getItem() instanceof PlayerHeadItem) {
            return false;
         }

         String string = s.getName().getString();
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

   public static int getItemSlot(Item item) {
      for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
         ItemStack itemStack = (ItemStack)mc.player.getInventory().main.get(i);
         if (itemStack.getItem() == item) {
            return i;
         }
      }

      return -1;
   }

   public static ItemStack getBestProjectile() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.getStack().isEmpty()
               && (item.getStack().getItem() == Items.EGG || item.getStack().getItem() == Items.SNOWBALL)
               && isItemValid(item.getStack())
         )
         .sorted(Comparator.comparingDouble(s -> -s.getStack().getCount()))
         .filter(s -> s.inventory != mc.player.getInventory())
         .<ItemStack>map(Slot::getStack)
         .findFirst()
         .orElse(null);
   }

   public static ItemStack getFishingRod() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof FishingRodItem && isItemValid(item.getStack()))
         .filter(s -> s.inventory != mc.player.getInventory())
         .<ItemStack>map(Slot::getStack)
         .findFirst()
         .orElse(null);
   }

   public static ItemStack getBestBlock() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.getStack().isEmpty()
               && item.getStack().getItem() instanceof BlockItem b
               && !Scaffold.invalidBlocks.contains(b.getBlock())
               && isItemValid(item.getStack())
         )
         .sorted(Comparator.comparingDouble(s -> -s.getStack().getCount()))
         .filter(s -> s.inventory != mc.player.getInventory())
         .<ItemStack>map(Slot::getStack)
         .findFirst()
         .orElse(null);
   }

   public static float getBestPickaxeScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof PickaxeItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> getToolScore(s.getStack()))
         .findFirst()
         .orElse(0.0F);
   }

   public static ItemStack getBestPickaxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof PickaxeItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .<ItemStack>map(Slot::getStack)
         .findFirst()
         .orElse(null);
   }

   public static float getBestAxeScore() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.getStack().isEmpty()
               && item.getStack().getItem() instanceof AxeItem
               && !isSharpnessAxe(item.getStack())
               && isItemValid(item.getStack())
         )
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> getToolScore(s.getStack()))
         .findFirst()
         .orElse(0.0F);
   }

   public static ItemStack getBestAxe() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.getStack().isEmpty()
               && item.getStack().getItem() instanceof AxeItem
               && !isSharpnessAxe(item.getStack())
               && isItemValid(item.getStack())
         )
         .sorted(Comparator.comparingInt(s -> -((int)(getToolScore(s.getStack()) * 100.0F))))
         .filter(s -> s.inventory != mc.player.getInventory())
         .<ItemStack>map(Slot::getStack)
         .findFirst()
         .orElse(null);
   }

   public static ItemStack getBestShapeAxe() {
      return getAllItems()
         .stream()
         .filter(
            item -> !item.getStack().isEmpty()
               && item.getStack().getItem() instanceof AxeItem
               && isSharpnessAxe(item.getStack())
               && isItemValid(item.getStack())
               && !isGodAxe(item.getStack())
         )
         .sorted(Comparator.comparingInt(s -> -((int)(getAxeDamage(s.getStack()) * 100.0F))))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> s.getStack())
         .findFirst()
         .orElse(null);
   }

   public static float getBestShovelScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof ShovelItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingDouble(s -> -getToolScore(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> getToolScore(s.getStack()))
         .findFirst()
         .orElse(0.0F);
   }

   public static ItemStack getBestShovel() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof ShovelItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingInt(s -> -((int)(getAxeDamage(s.getStack()) * 100.0F))))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> s.getStack())
         .findFirst()
         .orElse(null);
   }

   public static float getBestCrossbowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof CrossbowItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingDouble(s -> -getCrossbowScore(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> getCrossbowScore(s.getStack()))
         .findFirst()
         .orElse(0.0F);
   }

   public static ItemStack getBestCrossbow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof CrossbowItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingInt(s -> -((int)(getCrossbowScore(s.getStack()) * 100.0F))))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> s.getStack())
         .findFirst()
         .orElse(null);
   }

   public static float getBestPunchBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof BowItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingDouble(s -> -getPunchBowScore(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> getPunchBowScore(s.getStack()))
         .findFirst()
         .orElse(0.0F);
   }

   public static ItemStack getBestPunchBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof BowItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingInt(s -> -((int)(getPunchBowScore(s.getStack()) * 100.0F))))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> s.getStack())
         .findFirst()
         .orElse(null);
   }

   public static float getBestPowerBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof BowItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingDouble(s -> -getPowerBowScore(s.getStack())))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> getPowerBowScore(s.getStack()))
         .findFirst()
         .orElse(0.0F);
   }

   public static ItemStack getBestPowerBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() instanceof BowItem && isItemValid(item.getStack()))
         .sorted(Comparator.comparingInt(s -> -((int)(getPowerBowScore(s.getStack()) * 100.0F))))
         .filter(s -> s.inventory != mc.player.getInventory())
         .map(s -> s.getStack())
         .findFirst()
         .orElse(null);
   }

   public static int getItemCount(Item checkItem) {
      return getAllItems()
         .stream()
         .filter(s -> s.inventory != mc.player.getInventory())
         .filter(item -> !item.getStack().isEmpty() && item.getStack().getItem() == checkItem)
         .mapToInt(s -> s.getStack().getCount())
         .sum();
   }

   public static float getPunchBowScore(ItemStack stack) {
      if (stack == null) {
         return 0.0F;
      } else if (stack.isEmpty()) {
         return 0.0F;
      } else if (stack.getItem() instanceof BowItem) {
         float valence = 10.0F;
         valence += EnchantmentUtils.getLevel(Enchantments.PUNCH, stack);
         valence += EnchantmentUtils.getLevel(Enchantments.INFINITY, stack);
         valence += EnchantmentUtils.getLevel(Enchantments.FLAME, stack);
         valence += EnchantmentUtils.getLevel(Enchantments.POWER, stack) / 10.0F;
         return valence + (float)stack.getDamage() / stack.getMaxDamage();
      } else {
         return 0.0F;
      }
   }

   public static float getPowerBowScore(ItemStack stack) {
      if (stack == null) {
         return 0.0F;
      } else if (stack.isEmpty()) {
         return 0.0F;
      } else if (stack.getItem() instanceof BowItem) {
         float valence = 10.0F;
         valence += EnchantmentUtils.getLevel(Enchantments.PUNCH, stack) / 10.0F;
         valence += EnchantmentUtils.getLevel(Enchantments.INFINITY, stack);
         valence += EnchantmentUtils.getLevel(Enchantments.FLAME, stack);
         valence += EnchantmentUtils.getLevel(Enchantments.POWER, stack);
         return valence + (float)stack.getDamage() / stack.getMaxDamage();
      } else {
         return 0.0F;
      }
   }

   public static float getToolScore(ItemStack stack) {
      float valence = 0.0F;
      if (stack == null) {
         return 0.0F;
      } else if (stack.isEmpty()) {
         return 0.0F;
      } else if (isGodItem(stack)) {
         return 0.0F;
      } else if (isSharpnessAxe(stack)) {
         return 0.0F;
      } else {
         if (stack.getItem() instanceof PickaxeItem) {
            valence += stack.getMiningSpeedMultiplier(Blocks.STONE.getDefaultState());
         } else if (stack.getItem() instanceof AxeItem) {
            valence += stack.getMiningSpeedMultiplier(Blocks.OAK_LOG.getDefaultState());
         } else {
            if (!(stack.getItem() instanceof ShovelItem)) {
               return 0.0F;
            }

            valence += stack.getMiningSpeedMultiplier(Blocks.DIRT.getDefaultState());
         }

         int efficiency = EnchantmentUtils.getLevel(Enchantments.EFFICIENCY, stack);
         if (efficiency > 0) {
            valence += efficiency * 0.0075F;
         }

         return valence;
      }
   }

   public static float getAxeDamage(ItemStack stack) {
      float valence = 0.0F;
      if (stack == null) {
         return 0.0F;
      } else if (stack.isEmpty()) {
         return 0.0F;
      } else {
         if (stack.getItem() instanceof AxeItem && isSharpnessAxe(stack)) {
            AxeItem axe = (AxeItem)stack.getItem();
            if (axe == Items.WOODEN_AXE) {
               valence += 4.0F;
            } else if (axe == Items.STONE_AXE) {
               valence += 5.0F;
            } else if (axe == Items.IRON_AXE) {
               valence += 6.0F;
            } else if (axe == Items.GOLDEN_AXE) {
               valence += 4.0F;
            } else if (axe == Items.DIAMOND_AXE) {
               valence += 7.0F;
            }
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.0F + (itemEnchantmentLevel - 1) * 0.5F;
         }

         return valence;
      }
   }

   public static float getSwordDamage(ItemStack stack) {
      float valence = 0.0F;
      if (stack == null) {
         return 0.0F;
      } else if (stack.isEmpty()) {
         return 0.0F;
      } else {
         if (stack.getItem() instanceof SwordItem) {
            valence += stack.getDamage() + 1.0F;
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.0F + (itemEnchantmentLevel - 1) * 0.5F;
         }

         return valence;
      }
   }

   public static float getProtection(ItemStack itemStack) {
      int valence = 0;
      if (itemStack == null) {
         return 0.0F;
      } else if (itemStack.isEmpty()) {
         return 0.0F;
      } else {
         if (itemStack.getItem() instanceof ArmorItem armor) {
            RegistryKey<EquipmentAsset> assets = ItemUtils.getArmorAsset(itemStack);
            if (assets != null) {
               if (EquipmentAssetKeys.LEATHER.equals(assets)) {
                  valence += 100;
               } else if (EquipmentAssetKeys.CHAINMAIL.equals(assets)) {
                  valence += 200;
               } else if (EquipmentAssetKeys.IRON.equals(assets)) {
                  valence += 400;
               } else if (EquipmentAssetKeys.GOLD.equals(assets)) {
                  valence += 300;
               } else if (EquipmentAssetKeys.DIAMOND.equals(assets)) {
                  valence += 500;
               } else if (EquipmentAssetKeys.NETHERITE.equals(assets)) {
                  valence += 600;
               }
            }
         }

         valence += EnchantmentUtils.getLevel(Enchantments.PROTECTION, itemStack);
         return valence;
      }
   }

   public static float getCrossbowScore(ItemStack stack) {
      int valence = 0;
      if (stack == null) {
         return 0.0F;
      } else if (stack.isEmpty()) {
         return 0.0F;
      } else {
         if (stack.getItem() instanceof CrossbowItem) {
            valence += EnchantmentUtils.getLevel(Enchantments.QUICK_CHARGE, stack);
            valence += EnchantmentUtils.getLevel(Enchantments.MULTISHOT, stack);
            valence += EnchantmentUtils.getLevel(Enchantments.PIERCING, stack);
         }

         return valence;
      }
   }

   public static boolean isGodItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (stack.getItem() instanceof AxeItem
         && stack.getItem() == Items.GOLDEN_AXE
         && EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack) > 100) {
         return true;
      } else if (stack.getItem() == Items.SLIME_BALL && EnchantmentUtils.getLevel(Enchantments.KNOCKBACK, stack) > 1) {
         return true;
      } else {
         return stack.getItem() == Items.TOTEM_OF_UNDYING ? true : stack.getItem() == Items.END_CRYSTAL;
      }
   }

   public static boolean isCommonItemUseful(ItemStack stack) {
      if (stack.isEmpty()) {
         return true;
      } else {
         Item item = stack.getItem();
         if (item instanceof BlockItem block) {
            if (block.getBlock() == Blocks.ENCHANTING_TABLE) {
               return false;
            }

            if (block.getBlock() == Blocks.COBWEB) {
               return false;
            }
         } else {
            if (item instanceof WrittenBookItem) {
               return false;
            }

            if (item instanceof WritableBookItem) {
               return false;
            }

            if (item instanceof ExperienceBottleItem) {
               return false;
            }

            if (item instanceof FireworkRocketItem) {
               return false;
            }

            if (item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS || item == Items.MELON_SEEDS || item == Items.PUMPKIN_SEEDS) {
               return false;
            }

            if (item == Items.FLINT_AND_STEEL) {
               return false;
            }
         }

         return true;
      }
   }
}
