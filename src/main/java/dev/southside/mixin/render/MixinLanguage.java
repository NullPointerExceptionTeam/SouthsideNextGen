package dev.southside.mixin.render;

import dev.southside.utils.render.TextReplacementUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2477;
import net.minecraft.class_5481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_2477.class})
public abstract class MixinLanguage {
   @Inject(
      method = {"method_30933(Ljava/util/List;)Ljava/util/List;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void southside$wrapOrderedTextCache(CallbackInfoReturnable<List<class_5481>> cir) {
      List<class_5481> originalOrderedText = (List<class_5481>)cir.getReturnValue();
      List<class_5481> replacedText = new ArrayList<>();

      for (class_5481 text : originalOrderedText) {
         if (text != null) {
            replacedText.add(new TextReplacementUtils.ReplacementOrderedText(text));
         } else {
            replacedText.add(null);
         }
      }

      cir.setReturnValue(replacedText);
   }
}
