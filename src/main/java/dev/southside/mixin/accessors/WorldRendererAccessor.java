package dev.southside.mixin.accessors;

import net.minecraft.class_4604;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_761.class})
public interface WorldRendererAccessor {
   @Accessor("field_27740")
   class_4604 getFrustum();
}
