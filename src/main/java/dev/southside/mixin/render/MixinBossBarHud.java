package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.class_337;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_337.class})
public class MixinBossBarHud {
   @Inject(
      method = {"method_1796(Lnet/minecraft/class_332;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void render(CallbackInfo ci) {
      if (Southside.get().getModuleManager().get(NoRender.class).isEnabled() && Southside.get().getModuleManager().get(NoRender.class).bossbar.getValue()) {
         ci.cancel();
      }
   }
}
