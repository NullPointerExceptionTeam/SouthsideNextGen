package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.RawMouseEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public class MixinMouse {
   @Shadow
   @Final
   private MinecraftClient client;

   @Inject(
      method = {"onMouseButton(JIII)V"},
      at = {@At("TAIL")}
   )
   private void onMouseButtonPost(long handle, int button, int action, int mods, CallbackInfo ci) {
      if (handle == this.client.getWindow().getHandle()) {
         Southside.get().getEventBus().post(new RawMouseEvent(button, action, mods));
      }
   }
}
