package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.event.events.UpdateFoVEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayerEntity.class})
public abstract class MixinAbstractClientPlayerEntity {
   @Inject(
      method = {"getFovMultiplier(ZF)F"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void hookFoV(CallbackInfoReturnable<Float> cir) {
      Float returnValue = (Float)cir.getReturnValue();
      UpdateFoVEvent event = new UpdateFoVEvent(returnValue);
      Southside.get().getEventBus().call(event);
      cir.setReturnValue(event.getFov());
   }
}
