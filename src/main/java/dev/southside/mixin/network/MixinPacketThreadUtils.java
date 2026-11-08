package dev.southside.mixin.network;

import dev.southside.utils.network.ProtectionUtils;
import net.minecraft.class_1255;
import net.minecraft.class_2547;
import net.minecraft.class_2596;
import net.minecraft.class_2600;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2600.class})
public class MixinPacketThreadUtils {
   @Shadow
   @Final
   private static Logger field_20318;

   @Inject(
      method = {"method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_1255;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static <T extends class_2547> void onForceMainThreadNew(class_2596<T> packet, T listener, class_1255<?> threadExecutor, CallbackInfo ci) {
      handle(packet, listener, threadExecutor, ci);
   }

   private static <T extends class_2547> void handle(class_2596<T> packet, T listener, class_1255<?> executor, CallbackInfo ci) {
      ci.cancel();
      ProtectionUtils.onEnsureRunningOnSameThread(field_20318, packet, listener, executor);
   }
}
