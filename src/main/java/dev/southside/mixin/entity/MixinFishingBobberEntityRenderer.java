package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.class_10026;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_906;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_906.class})
public class MixinFishingBobberEntityRenderer {
   @Inject(
      method = {"method_3974(Lnet/minecraft/class_10026;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderHead(class_10026 fishingBobberEntityState, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo ci) {
      if (Southside.INSTANCE.getModuleManager().get(NoRender.class).fishingHook.getValue()) {
         ci.cancel();
      }
   }
}
