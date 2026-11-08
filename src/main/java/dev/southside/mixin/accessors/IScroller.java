package dev.southside.mixin.accessors;

import net.minecraft.class_9928;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_9928.class})
public interface IScroller {
   @Accessor("field_52764")
   double getCumulHorizontal();

   @Accessor("field_52765")
   double getCumulVertical();
}
