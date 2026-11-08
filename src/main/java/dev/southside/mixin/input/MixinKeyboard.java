package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.KeyPressEvent;
import dev.southside.event.events.RawKeyEvent;
import net.minecraft.class_309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_309.class})
public class MixinKeyboard {
   @Shadow
   @Final
   private class_310 field_1678;

   @Inject(
      method = {"method_1466(JIIII)V"},
      at = {@At("HEAD")}
   )
   public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      if (window == this.field_1678.method_22683().method_4490() && class_310.method_1551().field_1755 == null) {
         Southside.INSTANCE.getEventBus().call(new RawKeyEvent(key, scancode, action, modifiers));
         if (action == 1 && key != -1) {
            Southside.INSTANCE.getEventBus().call(new KeyPressEvent(key));
         }
      }
   }
}
