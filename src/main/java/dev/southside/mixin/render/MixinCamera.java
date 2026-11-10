package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.ViewClip;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Camera.class})
public class MixinCamera {
   @Inject(
      method = {"clipToSpace(F)F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onClipToSpaceHead(float f, CallbackInfoReturnable<Float> cir) {
      ViewClip viewClip = Southside.get().getModuleManager().get(ViewClip.class);
      if (viewClip != null && viewClip.isEnabled()) {
         float modifiedDistance = f * viewClip.scale.getValue().floatValue();
         cir.setReturnValue(modifiedDistance);
         cir.cancel();
      }
   }
}
