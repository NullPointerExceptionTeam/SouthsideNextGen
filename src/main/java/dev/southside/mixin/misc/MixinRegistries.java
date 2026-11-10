package dev.southside.mixin.misc;

import dev.southside.sound.Sounds;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Registries.class})
public class MixinRegistries {
   @Inject(
      method = {"bootstrap()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/registry/Registries;init()V"
      )}
   )
   private static void onInit(CallbackInfo ci) {
      Sounds.init();
   }
}
