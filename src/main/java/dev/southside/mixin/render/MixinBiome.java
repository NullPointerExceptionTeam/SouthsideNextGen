package dev.southside.mixin.render;

import dev.southside.module.impl.render.WorldManager;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_1959.class_1963;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1959.class})
public class MixinBiome {
   @Inject(
      method = {"method_48163()Z"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onHasPrecipitation(CallbackInfoReturnable<Boolean> cir) {
      if (WorldManager.forceSnow || WorldManager.forceRain) {
         cir.setReturnValue(true);
      }
   }

   @Inject(
      method = {"method_48162(Lnet/minecraft/class_2338;I)Lnet/minecraft/class_1959$class_1963;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetPrecipitation(class_2338 pos, int i, CallbackInfoReturnable<class_1963> cir) {
      if (WorldManager.forceSnow) {
         cir.setReturnValue(class_1963.field_9383);
      } else if (WorldManager.forceRain) {
         cir.setReturnValue(class_1963.field_9382);
      }
   }

   @Inject(
      method = {"method_39927(Lnet/minecraft/class_2338;I)Z"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onDoesNotSnow(class_2338 pos, int i, CallbackInfoReturnable<Boolean> cir) {
      if (WorldManager.forceSnow) {
         cir.setReturnValue(false);
      }
   }
}
