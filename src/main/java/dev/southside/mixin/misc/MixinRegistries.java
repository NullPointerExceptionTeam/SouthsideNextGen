package dev.southside.mixin.misc;

import dev.southside.sound.Sounds;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_7923.class})
public class MixinRegistries {
   @Inject(
      method = {"method_47476()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_7923;method_47487()V"
      )}
   )
   private static void onInit(CallbackInfo ci) {
      Sounds.init();
   }
}
