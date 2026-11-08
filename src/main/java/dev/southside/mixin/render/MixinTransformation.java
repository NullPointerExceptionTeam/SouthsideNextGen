package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.ApplyTransformationEvent;
import net.minecraft.class_4587;
import net.minecraft.class_804;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_804.class})
public abstract class MixinTransformation {
   @Inject(
      method = {"method_23075(ZLnet/minecraft/class_4587;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onApply(boolean leftHanded, class_4587 matrices, CallbackInfo info) {
      ApplyTransformationEvent event = Southside.INSTANCE.getEventBus().post(ApplyTransformationEvent.get((class_804)(Object)this, leftHanded, matrices));
      if (event.isCancelled()) {
         info.cancel();
      }
   }
}
