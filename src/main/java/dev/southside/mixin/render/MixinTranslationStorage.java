package dev.southside.mixin.render;

import dev.southside.utils.render.TextReplacementUtils;
import net.minecraft.class_1078;
import net.minecraft.class_5481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1078.class})
public class MixinTranslationStorage {
   @Inject(
      method = {"method_30934(Lnet/minecraft/class_5348;)Lnet/minecraft/class_5481;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void southside$wrapOrderedTextCache(CallbackInfoReturnable<class_5481> cir) {
      class_5481 originalOrderedText = (class_5481)cir.getReturnValue();
      if (originalOrderedText != null) {
         cir.setReturnValue(new TextReplacementUtils.ReplacementOrderedText(originalOrderedText));
      }
   }
}
