package dev.southside.mixin.render;

import dev.southside.utils.render.TextReplacementUtils;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({TranslationStorage.class})
public class MixinTranslationStorage {
   @Inject(
      method = {"reorder(Lnet/minecraft/text/StringVisitable;)Lnet/minecraft/text/OrderedText;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void southside$wrapOrderedTextCache(CallbackInfoReturnable<OrderedText> cir) {
      OrderedText originalOrderedText = (OrderedText)cir.getReturnValue();
      if (originalOrderedText != null) {
         cir.setReturnValue(new TextReplacementUtils.ReplacementOrderedText(originalOrderedText));
      }
   }
}
