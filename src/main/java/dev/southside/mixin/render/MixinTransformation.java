package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.ApplyTransformationEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.model.json.Transformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Transformation.class})
public abstract class MixinTransformation {
   @Inject(
      method = {"apply(ZLnet/minecraft/client/util/math/MatrixStack;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onApply(boolean leftHanded, MatrixStack matrices, CallbackInfo info) {
      ApplyTransformationEvent event = Southside.INSTANCE.getEventBus().post(ApplyTransformationEvent.get((Transformation)(Object)this, leftHanded, matrices));
      if (event.isCancelled()) {
         info.cancel();
      }
   }
}
