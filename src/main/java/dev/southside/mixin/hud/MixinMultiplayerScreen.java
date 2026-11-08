package dev.southside.mixin.hud;

import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_500.class})
public class MixinMultiplayerScreen {
   @Inject(
      method = {"method_25426()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_500;method_20121()V"
      )}
   )
   public void multiplayerGuiOpen(CallbackInfo ci) {
   }
}
