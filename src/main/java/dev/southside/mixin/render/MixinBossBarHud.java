package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BossBarHud.class})
public class MixinBossBarHud {
   @Inject(
      method = {"render(Lnet/minecraft/client/gui/DrawContext;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void render(CallbackInfo ci) {
      if (Southside.get().getModuleManager().get(NoRender.class).isEnabled() && Southside.get().getModuleManager().get(NoRender.class).bossbar.getValue()) {
         ci.cancel();
      }
   }
}
