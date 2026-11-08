package dev.southside.utils.player;

import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_5321;
import net.minecraft.class_6880;

public class EnchantmentUtils {
   public static int getLevel(class_5321<class_1887> enchantment, class_1799 stack) {
      if (stack == null) {
         return 0;
      } else {
         Optional<class_6880<class_1887>> entry = stack.method_58657().method_57534().stream().filter(e -> e.method_40225(enchantment)).findFirst();
         return entry.<Integer>map(enchantmentRegistryEntry -> class_1890.method_8225(enchantmentRegistryEntry, stack)).orElse(0);
      }
   }
}
