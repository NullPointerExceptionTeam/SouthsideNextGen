package dev.southside.mixin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.southside.Southside;
import dev.southside.utils.ChatUtils;
import net.minecraft.class_2558;
import net.minecraft.class_2583;
import net.minecraft.class_437;
import net.minecraft.class_2558.class_2559;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_437.class})
public class MixinScreen {
   @Inject(
      method = {"method_25430(Lnet/minecraft/class_2583;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void handleSouthsideCommandClick(class_2583 style, CallbackInfoReturnable<Boolean> cir) {
      if (style != null && style.method_10970() != null) {
         class_2558 clickEvent = style.method_10970();
         if (clickEvent.method_10845() == class_2559.field_11750 && clickEvent.method_10844().startsWith(".")) {
            try {
               Southside.get().getCommandManager().dispatch(clickEvent.method_10844().substring(1));
            } catch (CommandSyntaxException var5) {
               ChatUtils.error(var5.getMessage());
            }

            cir.setReturnValue(true);
         }
      }
   }
}
