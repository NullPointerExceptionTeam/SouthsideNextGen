package dev.southside.mixin.accessors;

import net.minecraft.class_1496;
import net.minecraft.class_1724;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_1724.class})
public interface IHorseScreenHandler {
   @Accessor("field_7837")
   class_1496 getEntity();
}
