package dev.southside.utils.player;

import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantmentUtils {
   public static int getLevel(RegistryKey<Enchantment> enchantment, ItemStack stack) {
      if (stack == null) {
         return 0;
      } else {
         Optional<RegistryEntry<Enchantment>> entry = stack.getEnchantments().getEnchantments().stream().filter(e -> e.matchesKey(enchantment)).findFirst();
         return entry.<Integer>map(enchantmentRegistryEntry -> EnchantmentHelper.getLevel(enchantmentRegistryEntry, stack)).orElse(0);
      }
   }
}
