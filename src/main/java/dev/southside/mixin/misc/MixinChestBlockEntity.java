package dev.southside.mixin.misc;

import dev.southside.Southside;
import dev.southside.event.events.ChestOpenedEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChestBlockEntity.class})
public class MixinChestBlockEntity {
   @Inject(
      method = {"onOpen(Lnet/minecraft/entity/player/PlayerEntity;)V"},
      at = {@At("HEAD")}
   )
   public void onOpen(PlayerEntity player, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().post(new ChestOpenedEvent(player, ((ChestBlockEntity)(Object)this).getPos()));
   }
}
