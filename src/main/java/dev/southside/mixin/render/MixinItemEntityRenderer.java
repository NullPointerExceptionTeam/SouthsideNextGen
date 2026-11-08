package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.RenderItemEntityEvent;
import net.minecraft.class_10039;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5819;
import net.minecraft.class_916;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_916.class})
public abstract class MixinItemEntityRenderer {
   @Shadow
   @Final
   private class_5819 field_4725;

   @Inject(
      method = {"method_3996(Lnet/minecraft/class_10039;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void render(class_10039 itemEntityRenderState, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo ci) {
      RenderItemEntityEvent event = Southside.INSTANCE
         .getEventBus()
         .post(RenderItemEntityEvent.get(itemEntityRenderState, matrixStack, vertexConsumerProvider, i));
      if (event.isCancelled()) {
         ci.cancel();
      }
   }
}
