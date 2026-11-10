package dev.southside.mixin.accessors;

import net.minecraft.client.input.Scroller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Scroller.class})
public interface IScroller {
   @Accessor("cumulHorizontal")
   double getCumulHorizontal();

   @Accessor("cumulVertical")
   double getCumulVertical();
}
