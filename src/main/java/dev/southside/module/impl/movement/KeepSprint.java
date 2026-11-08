package dev.southside.module.impl.movement;

import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.player.EnchantmentUtils;
import net.minecraft.class_1893;
import net.minecraft.class_310;

public class KeepSprint extends Module {
   public KeepSprint() {
      super(Category.Movement, "KeepSprint");
   }

   public static boolean canKeepSprint() {
      return mc.field_1724 != null
         && Southside.INSTANCE.getModuleManager().get(KeepSprint.class).isEnabled()
         && EnchantmentUtils.getLevel(class_1893.field_9121, class_310.method_1551().field_1724.method_6047()) <= 0
         && EnchantmentUtils.getLevel(class_1893.field_9121, class_310.method_1551().field_1724.method_6079()) <= 0;
   }
}
