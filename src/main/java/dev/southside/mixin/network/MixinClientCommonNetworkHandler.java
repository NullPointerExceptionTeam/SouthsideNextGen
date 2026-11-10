package dev.southside.mixin.network;

import net.minecraft.network.packet.Packet;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientCommonNetworkHandler.class})
public class MixinClientCommonNetworkHandler {
   @Inject(
      method = {"onPacketException(Lnet/minecraft/network/packet/Packet;Ljava/lang/Exception;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hookSilentThrows(Packet packet, Exception exception, CallbackInfo ci) {
      exception.printStackTrace();
      ci.cancel();
   }
}
