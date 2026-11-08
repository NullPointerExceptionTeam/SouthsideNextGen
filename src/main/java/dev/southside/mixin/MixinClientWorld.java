package dev.southside.mixin;

import dev.southside.Southside;
import dev.southside.event.events.AddEntityEvent;
import dev.southside.event.events.RemoveEntityEvent;
import net.minecraft.class_1297;
import net.minecraft.class_638;
import net.minecraft.class_1297.class_5529;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_638.class})
public class MixinClientWorld {
   @Inject(
      method = {"method_53875(Lnet/minecraft/class_1297;)V"},
      at = {@At("RETURN")}
   )
   private void onAddEntity(class_1297 entity, CallbackInfo info) {
      Southside.INSTANCE.getEventBus().call(new AddEntityEvent(entity, (class_638)(Object)this));
   }

   @Inject(
      method = {"method_2945(ILnet/minecraft/class_1297$class_5529;)V"},
      at = {@At("HEAD")}
   )
   private void onRemoveEntity(int entityId, class_5529 reason, CallbackInfo info) {
      class_638 world = (class_638)(Object)this;
      class_1297 entity = world.method_8469(entityId);
      Southside.INSTANCE.getEventBus().call(new RemoveEntityEvent(entity, reason, world));
   }
}
