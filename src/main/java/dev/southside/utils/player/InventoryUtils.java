package dev.southside.utils.player;

import dev.southside.module.impl.movement.NoSlowTest;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.item.ItemUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.util.Hand;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ArrowItem;
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
import net.minecraft.world.BlockView;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.BlockState;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;

public class InventoryUtils {
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public static boolean shouldDisableFeatures() {
      return NoSlowTest.shouldEnable ? true : getAllItems().stream().anyMatch(item -> {
         if (item.isEmpty()) {
            return false;
         } else {
            String string = item.getName().getString();
            return string.contains("长按点击") || string.contains("点击使用") || string.contains("离开游戏") || string.contains("选择一个队伍") || string.contains("再来一局");
         }
      });
   }

   public static boolean isGoldenHead(ItemStack e) {
      if (e.isEmpty()) {
         return false;
      } else {
         if (e.getItem() instanceof BlockItem) {
            BlockItem item = (BlockItem)e.getItem();
            if (item.getBlock() instanceof SkullBlock) {
               return true;
            }
         }

         return false;
      }
   }

   public static int findItem(int startSlot, int endSlot, Item item) {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
               return i;
            }
         }

         return -1;
      }
   }

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
         } else {
            return stack.getComponents() != null && stack.getComponents().toString().contains("一刀")
               ? true
               : stack.getItem() == Items.GOLDEN_AXE && EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack) > 100;
         }
      }
   }

   public static boolean isEnchantedGApple(ItemStack stack) {
      return stack.isEmpty() ? false : stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
   }

   public static boolean isEndCrystal(ItemStack stack) {
      return stack.isEmpty() ? false : stack.getItem() == Items.END_CRYSTAL;
   }

   public static boolean isKBBall(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         return stack.getItem() != Items.SLIME_BALL ? false : !stack.contains(DataComponentTypes.CUSTOM_NAME);
      }
   }

   public static boolean isKBStick(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         return stack.getItem() != Items.STICK ? false : EnchantmentUtils.getLevel(Enchantments.KNOCKBACK, stack) > 1;
      }
   }

   public static int findEmptyInventory() {
      for (int i = 9; i < mc.player.getInventory().main.size(); i++) {
         if (((ItemStack)mc.player.getInventory().main.get(i)).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public static int findEmptySlot() {
      for (int i = 0; i < 9; i++) {
         if (((ItemStack)mc.player.getInventory().main.get(i)).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public static int getPunchLevel(ItemStack stack) {
      return EnchantmentUtils.getLevel(Enchantments.PUNCH, stack);
   }

   public static int getPowerLevel(ItemStack stack) {
      return EnchantmentUtils.getLevel(Enchantments.POWER, stack);
   }

   public static List<ItemStack> getAllItems() {
      ArrayList<ItemStack> list = new ArrayList<>(40);
      list.addAll(mc.player.getInventory().main);
      list.addAll(mc.player.getInventory().armor);
      return list;
   }

   public static boolean isInvalidBlock(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().getDefaultState();
            ClientWorld world = MinecraftClient.getInstance().world;
            BlockView view = (BlockView)(world != null ? world : EmptyBlockView.INSTANCE);
            return !state.isFullCube(view, BlockPos.ORIGIN) || Scaffold.invalidBlocks.contains(blockItem.getBlock());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static float getBestArmorScore(EquipmentSlot slot) {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof ArmorItem && ItemUtils.getEquippableSlotType(item) == slot)
         .map(InventoryUtils::getProtection)
         .max(Float::compareTo)
         .orElse(0.0F);
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
         .filter(item -> !item.isEmpty() && item.getItem() instanceof SwordItem)
         .map(InventoryUtils::getSwordDamage)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestSword() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof SwordItem)
         .max(Comparator.comparingInt(s -> (int)(getSwordDamage(s) * 100.0F)))
         .orElse(null);
   }

   public static int getItemStackSlot(ItemStack stack) {
      if (stack == null) {
         return -1;
      } else {
         for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            if (mc.player.getInventory().main.get(i) == stack) {
               return i;
            }
         }

         return -1;
      }
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
            item -> !item.isEmpty() && (item.getItem() == Items.EGG || item.getItem() == Items.SNOWBALL) && isItemValid(item)
         )
         .max(Comparator.comparingInt(ItemStack::getCount))
         .orElse(null);
   }

   public static ItemStack getFishingRod() {
      return getAllItems().stream().filter(item -> !item.isEmpty() && item.getItem() instanceof FishingRodItem && isItemValid(item)).findAny().orElse(null);
   }

   public static int getBlockCountInInventory() {
      return getAllItems()
         .stream()
         .filter(
            stack -> !stack.isEmpty()
               && stack.getItem() instanceof BlockItem
               && !stack.getItem().equals(Items.TNT)
               && !isInvalidBlock(stack)
               && isItemValid(stack)
         )
         .mapToInt(ItemStack::getCount)
         .sum();
   }

   public static ItemStack getWorstProjectile() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && (item.getItem() == Items.EGG || item.getItem() == Items.SNOWBALL))
         .min(Comparator.comparingInt(ItemStack::getCount))
         .orElse(null);
   }

   public static ItemStack getWorstArrow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof ArrowItem && isItemValid(item))
         .min(Comparator.comparingInt(ItemStack::getCount))
         .orElse(null);
   }

   public static ItemStack getWorstBlock() {
      return getAllItems()
         .stream()
         .filter(stack -> !stack.isEmpty() && stack.getItem() instanceof BlockItem && !isInvalidBlock(stack) && isItemValid(stack))
         .min(Comparator.comparingInt(ItemStack::getCount))
         .orElse(null);
   }

   public static ItemStack getBestBlock() {
      return getAllItems()
         .stream()
         .filter(stack -> !stack.isEmpty() && stack.getItem() instanceof BlockItem && !isInvalidBlock(stack) && isItemValid(stack))
         .max(Comparator.comparingInt(ItemStack::getCount))
         .orElse(null);
   }

   public static float getBestPickaxeScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof PickaxeItem && isItemValid(item))
         .map(InventoryUtils::getToolScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestPickaxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof PickaxeItem && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getToolScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestAxeScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof AxeItem && !isSharpnessAxe(item) && isItemValid(item))
         .map(InventoryUtils::getToolScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestAxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof AxeItem && !isSharpnessAxe(item) && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getToolScore(s) * 100.0F)))
         .orElse(null);
   }

   public static ItemStack getBestShapeAxe() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof AxeItem && isSharpnessAxe(item) && isItemValid(item) && !isGodAxe(item))
         .max(Comparator.comparingInt(s -> (int)(getAxeDamage(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestShovelScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof ShovelItem && isItemValid(item))
         .map(InventoryUtils::getToolScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestShovel() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof ShovelItem && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getToolScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestCrossbowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof CrossbowItem && isItemValid(item))
         .map(InventoryUtils::getCrossbowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestCrossbow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof CrossbowItem && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getCrossbowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestPunchBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof BowItem && isItemValid(item))
         .map(InventoryUtils::getPunchBowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestPunchBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof BowItem && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getPunchBowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestPowerBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof BowItem && isItemValid(item))
         .map(InventoryUtils::getPowerBowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestPowerBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof BowItem && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getPowerBowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static float getBestFlameBowScore() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof BowItem && isItemValid(item))
         .map(InventoryUtils::getFlameBowScore)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   public static ItemStack getBestFlameBow() {
      return getAllItems()
         .stream()
         .filter(item -> !item.isEmpty() && item.getItem() instanceof BowItem && isItemValid(item))
         .max(Comparator.comparingInt(s -> (int)(getFlameBowScore(s) * 100.0F)))
         .orElse(null);
   }

   public static boolean isPunchBow(ItemStack stack) {
      return getPunchBowScore(stack) > 10.0F && isItemValid(stack);
   }

   public static boolean isPowerBow(ItemStack stack) {
      return getPowerBowScore(stack) > 10.0F && isItemValid(stack);
   }

   public static boolean isFlameBow(ItemStack stack) {
      return getFlameBowScore(stack) > 10.0F && isItemValid(stack);
   }

   public static boolean hasItem(Item checkItem) {
      return getAllItems().stream().anyMatch(item -> !item.isEmpty() && item.getItem() == checkItem);
   }

   public static int getItemCount(Item checkItem) {
      return getAllItems().stream().filter(item -> !item.isEmpty() && item.getItem() == checkItem).mapToInt(ItemStack::getCount).sum();
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

   public static float getFlameBowScore(ItemStack stack) {
      if (stack == null) {
         return 0.0F;
      } else if (stack.isEmpty()) {
         return 0.0F;
      } else if (stack.getItem() instanceof BowItem) {
         float valence = 10.0F;
         valence += EnchantmentUtils.getLevel(Enchantments.FLAME, stack) * 2.0F;
         valence += EnchantmentUtils.getLevel(Enchantments.INFINITY, stack);
         valence += EnchantmentUtils.getLevel(Enchantments.POWER, stack) / 10.0F;
         valence += EnchantmentUtils.getLevel(Enchantments.PUNCH, stack) / 10.0F;
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
            valence += (float)ItemUtils.handAttributeValueWithStack(stack, EntityAttributes.ATTACK_DAMAGE, Hand.MAIN_HAND);
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.25F * itemEnchantmentLevel;
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
            valence += (float)ItemUtils.handAttributeValueWithStack(stack, EntityAttributes.ATTACK_DAMAGE, Hand.MAIN_HAND);
         }

         int itemEnchantmentLevel = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
         if (itemEnchantmentLevel > 0) {
            valence += 1.25F * itemEnchantmentLevel;
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
         if (itemStack.getItem() instanceof ArmorItem) {
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
      return ContainerUtils.getCrossbowScore(stack);
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
         if (item instanceof WrittenBookItem) {
            return false;
         } else if (item instanceof WritableBookItem) {
            return false;
         } else if (item instanceof ExperienceBottleItem) {
            return false;
         } else if (item instanceof FireworkRocketItem) {
            return false;
         } else if (item.equals(Items.CHEST) || item.equals(Items.ANVIL) || item.equals(Items.ENCHANTING_TABLE)) {
            return false;
         } else {
            return item == Items.WHEAT_SEEDS
                  || item == Items.BEETROOT_SEEDS
                  || item == Items.MELON_SEEDS
                  || item == Items.PUMPKIN_SEEDS
                  || item == Items.TORCHFLOWER_SEEDS
               ? false
               : item != Items.FLINT_AND_STEEL;
         }
      }
   }
}
