package dev.southside.mixin.misc;

import dev.southside.mixininterfaces.IRenderTickCounter;
import dev.southside.utils.MinecraftTickTimerUtils;
import net.minecraft.client.render.RenderTickCounter.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Dynamic.class})
public class MixinRenderTickCounter implements IRenderTickCounter {
   @Mutable
   @Shadow
   @Final
   public float tickTime;
   @Shadow
   public float lastFrameDuration;

   @Override
   public void setTickTime(float tickTime) {
      this.tickTime = tickTime;
   }

   @Inject(
      method = {"beginRenderTick(J)I"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J",
         opcode = 181
      )}
   )
   private void onBeingRenderTick(long a, CallbackInfoReturnable<Integer> info) {
      this.lastFrameDuration = this.lastFrameDuration * MinecraftTickTimerUtils.getMultiplier();
   }
}
