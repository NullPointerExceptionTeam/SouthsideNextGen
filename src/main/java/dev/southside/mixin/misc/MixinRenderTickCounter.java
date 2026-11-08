package dev.southside.mixin.misc;

import dev.southside.mixininterfaces.IRenderTickCounter;
import dev.southside.utils.MinecraftTickTimerUtils;
import net.minecraft.class_9779.class_9781;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_9781.class})
public class MixinRenderTickCounter implements IRenderTickCounter {
   @Mutable
   @Shadow
   @Final
   public float field_51964;
   @Shadow
   public float field_51958;

   @Override
   public void setTickTime(float tickTime) {
      this.field_51964 = tickTime;
   }

   @Inject(
      method = {"method_60639(J)I"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_9779$class_9781;field_51962:J",
         opcode = 181
      )}
   )
   private void onBeingRenderTick(long a, CallbackInfoReturnable<Integer> info) {
      this.field_51958 = this.field_51958 * MinecraftTickTimerUtils.getMultiplier();
   }
}
