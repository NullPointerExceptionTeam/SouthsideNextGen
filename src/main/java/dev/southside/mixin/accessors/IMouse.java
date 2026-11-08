package dev.southside.mixin.accessors;

import net.minecraft.class_312;
import net.minecraft.class_9928;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_312.class})
public interface IMouse {
   @Accessor("field_52757")
   class_9928 getScroller();

   @Accessor("field_1789")
   double getCursorDeltaX();

   @Accessor("field_1787")
   double getCursorDeltaY();
}
