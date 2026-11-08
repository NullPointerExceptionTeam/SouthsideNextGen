package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.class_10034;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_572;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_970.class})
public class MixinArmorFeatureRenderer<S extends class_10034, M extends class_572<S>, A extends class_572<S>> {
   @Inject(
      method = {"method_4169(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1799;Lnet/minecraft/class_1304;ILnet/minecraft/class_572;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderArmor(class_4587 matrices, class_4597 vertexConsumers, class_1799 stack, class_1304 slot, int light, A armorModel, CallbackInfo ci) {
      if (Southside.get().getModuleManager().get(NoRender.class).isEnabled() && Southside.get().getModuleManager().get(NoRender.class).armor.getValue()) {
         ci.cancel();
      }
   }
}
