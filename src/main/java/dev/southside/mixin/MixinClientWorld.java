package dev.southside.mixin;

import dev.southside.Southside;
import dev.southside.event.events.AddEntityEvent;
import dev.southside.event.events.RemoveEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity.RemovalReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientWorld.class})
public class MixinClientWorld {
   @Inject(
      method = {"addEntity(Lnet/minecraft/entity/Entity;)V"},
      at = {@At("RETURN")}
   )
   private void onAddEntity(Entity entity, CallbackInfo info) {
      Southside.INSTANCE.getEventBus().call(new AddEntityEvent(entity, (ClientWorld)(Object)this));
   }

   @Inject(
      method = {"removeEntity(ILnet/minecraft/entity/Entity$RemovalReason;)V"},
      at = {@At("HEAD")}
   )
   private void onRemoveEntity(int entityId, RemovalReason reason, CallbackInfo info) {
      ClientWorld world = (ClientWorld)(Object)this;
      Entity entity = world.getEntityById(entityId);
      Southside.INSTANCE.getEventBus().call(new RemoveEntityEvent(entity, reason, world));
   }
}
