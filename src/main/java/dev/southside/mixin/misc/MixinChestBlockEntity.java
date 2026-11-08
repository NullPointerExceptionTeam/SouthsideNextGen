package dev.southside.mixin.misc;

import dev.southside.Southside;
import dev.southside.event.events.ChestOpenedEvent;
import net.minecraft.class_1657;
import net.minecraft.class_2595;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2595.class})
public class MixinChestBlockEntity {
   @Inject(
      method = {"method_5435(Lnet/minecraft/class_1657;)V"},
      at = {@At("HEAD")}
   )
   public void onOpen(class_1657 player, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().post(new ChestOpenedEvent(player, ((class_2595)(Object)this).method_11016()));
   }
}
