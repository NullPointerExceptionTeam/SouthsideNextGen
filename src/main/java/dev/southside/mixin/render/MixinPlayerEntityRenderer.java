package dev.southside.mixin.render;

import dev.southside.utils.render.WingsRenderer;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_3883;
import net.minecraft.class_591;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1007.class})
public class MixinPlayerEntityRenderer {
   @Inject(
      method = {"<init>(Lnet/minecraft/class_5617$class_5618;Z)V"},
      at = {@At("RETURN")}
   )
   private void addWingsLayer(class_5618 ctx, boolean slim, CallbackInfo ci) {
      class_1007 renderer = (class_1007)(Object)this;
      renderer.method_4046(new WingsRenderer((class_3883<class_10055, class_591>)this));
   }
}
