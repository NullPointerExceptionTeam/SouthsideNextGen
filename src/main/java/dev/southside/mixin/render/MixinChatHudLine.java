package dev.southside.mixin.render;

import dev.southside.utils.render.TextReplacementUtils;
import net.minecraft.text.OrderedText;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Visible.class})
public class MixinChatHudLine {
   @Mutable
   @Shadow
   @Final
   private OrderedText content;

   @Inject(
      method = {"<init>(ILnet/minecraft/text/OrderedText;Lnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;content:Lnet/minecraft/text/OrderedText;",
         opcode = 181,
         shift = Shift.AFTER
      )}
   )
   private void replace(int i, OrderedText orderedText, MessageIndicator messageIndicator, boolean bl, CallbackInfo ci) {
      this.content = new TextReplacementUtils.ReplacementOrderedText(this.content);
   }
}
