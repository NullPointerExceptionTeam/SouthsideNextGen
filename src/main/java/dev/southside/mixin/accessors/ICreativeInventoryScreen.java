package dev.southside.mixin.accessors;

import net.minecraft.class_1761;
import net.minecraft.class_481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_481.class})
public interface ICreativeInventoryScreen {
   @Accessor("field_2896")
   static class_1761 getSelectedTab() {
      return null;
   }
}
