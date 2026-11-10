package dev.southside.module.impl.movement;

import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.player.EnchantmentUtils;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.client.MinecraftClient;

public class KeepSprint extends Module {
   public KeepSprint() {
      super(Category.Movement, "KeepSprint");
   }

   public static boolean canKeepSprint() {
      return mc.player != null
         && Southside.INSTANCE.getModuleManager().get(KeepSprint.class).isEnabled()
         && EnchantmentUtils.getLevel(Enchantments.KNOCKBACK, MinecraftClient.getInstance().player.getMainHandStack()) <= 0
         && EnchantmentUtils.getLevel(Enchantments.KNOCKBACK, MinecraftClient.getInstance().player.getOffHandStack()) <= 0;
   }
}
