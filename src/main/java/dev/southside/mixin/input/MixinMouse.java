package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.RawMouseEvent;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public class MixinMouse {
   @Shadow
   @Final
   private class_310 field_1779;

   @Inject(
      method = {"method_1601(JIII)V"},
      at = {@At("TAIL")}
   )
   private void onMouseButtonPost(long handle, int button, int action, int mods, CallbackInfo ci) {
      if (handle == this.field_1779.method_22683().method_4490()) {
         Southside.get().getEventBus().post(new RawMouseEvent(button, action, mods));
      }
   }
}
