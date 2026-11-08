package dev.southside.mixin.block;

import dev.southside.Southside;
import dev.southside.event.events.EntityCobwebCollisionEvent;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2560;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2560.class})
public class MixinCobwebBlock {
   @Inject(
      method = {"method_9548(Lnet/minecraft/class_2680;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;Lnet/minecraft/class_1297;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onEntityCollision(class_2680 state, class_1937 world, class_2338 pos, class_1297 entity, CallbackInfo ci) {
      EntityCobwebCollisionEvent event = new EntityCobwebCollisionEvent(state, world, pos, entity);
      Southside.get().getEventBus().post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }
}
