package dev.southside.mixin.network;

import net.minecraft.class_2596;
import net.minecraft.class_8673;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_8673.class})
public class MixinClientCommonNetworkHandler {
   @Inject(
      method = {"method_59807(Lnet/minecraft/class_2596;Ljava/lang/Exception;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hookSilentThrows(class_2596 packet, Exception exception, CallbackInfo ci) {
      exception.printStackTrace();
      ci.cancel();
   }
}
