package dev.southside.mixin.accessors;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.Scroller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Mouse.class})
public interface IMouse {
   @Accessor("scroller")
   Scroller getScroller();

   @Accessor("cursorDeltaX")
   double getCursorDeltaX();

   @Accessor("cursorDeltaY")
   double getCursorDeltaY();
}
