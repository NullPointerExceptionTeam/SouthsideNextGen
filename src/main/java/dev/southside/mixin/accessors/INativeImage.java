package dev.southside.mixin.accessors;

import net.minecraft.class_1011;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_1011.class})
public interface INativeImage {
   @Accessor("field_4988")
   long getPointer();
}
