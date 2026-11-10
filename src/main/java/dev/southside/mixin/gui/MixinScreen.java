package dev.southside.mixin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.southside.Southside;
import dev.southside.utils.ChatUtils;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Screen.class})
public class MixinScreen {
   @Inject(
      method = {"handleTextClick(Lnet/minecraft/text/Style;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void handleSouthsideCommandClick(Style style, CallbackInfoReturnable<Boolean> cir) {
      if (style != null && style.getClickEvent() != null) {
         ClickEvent clickEvent = style.getClickEvent();
         if (clickEvent.getAction() == Action.RUN_COMMAND && clickEvent.getValue().startsWith(".")) {
            try {
               Southside.get().getCommandManager().dispatch(clickEvent.getValue().substring(1));
            } catch (CommandSyntaxException var5) {
               ChatUtils.error(var5.getMessage());
            }

            cir.setReturnValue(true);
         }
      }
   }
}
