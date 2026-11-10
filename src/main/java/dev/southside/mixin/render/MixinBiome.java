package dev.southside.mixin.render;

import dev.southside.module.impl.render.WorldManager;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome.Precipitation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Biome.class})
public class MixinBiome {
   @Inject(
      method = {"hasPrecipitation()Z"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onHasPrecipitation(CallbackInfoReturnable<Boolean> cir) {
      if (WorldManager.forceSnow || WorldManager.forceRain) {
         cir.setReturnValue(true);
      }
   }

   @Inject(
      method = {"getPrecipitation(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/world/biome/Biome$Precipitation;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetPrecipitation(BlockPos pos, int i, CallbackInfoReturnable<Precipitation> cir) {
      if (WorldManager.forceSnow) {
         cir.setReturnValue(Precipitation.SNOW);
      } else if (WorldManager.forceRain) {
         cir.setReturnValue(Precipitation.RAIN);
      }
   }

   @Inject(
      method = {"doesNotSnow(Lnet/minecraft/util/math/BlockPos;I)Z"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onDoesNotSnow(BlockPos pos, int i, CallbackInfoReturnable<Boolean> cir) {
      if (WorldManager.forceSnow) {
         cir.setReturnValue(false);
      }
   }
}
