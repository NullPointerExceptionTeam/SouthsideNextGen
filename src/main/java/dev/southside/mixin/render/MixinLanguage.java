package dev.southside.mixin.render;

import dev.southside.utils.render.TextReplacementUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Language;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Language.class})
public abstract class MixinLanguage {
   @Inject(
      method = {"reorder(Ljava/util/List;)Ljava/util/List;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void southside$wrapOrderedTextCache(CallbackInfoReturnable<List<OrderedText>> cir) {
      List<OrderedText> originalOrderedText = (List<OrderedText>)cir.getReturnValue();
      List<OrderedText> replacedText = new ArrayList<>();

      for (OrderedText text : originalOrderedText) {
         if (text != null) {
            replacedText.add(new TextReplacementUtils.ReplacementOrderedText(text));
         } else {
            replacedText.add(null);
         }
      }

      cir.setReturnValue(replacedText);
   }
}
