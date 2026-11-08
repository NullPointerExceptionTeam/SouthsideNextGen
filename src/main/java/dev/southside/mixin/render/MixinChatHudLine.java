package dev.southside.mixin.render;

import dev.southside.utils.render.TextReplacementUtils;
import net.minecraft.class_5481;
import net.minecraft.class_7591;
import net.minecraft.class_303.class_7590;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_7590.class})
public class MixinChatHudLine {
   @Mutable
   @Shadow
   @Final
   private class_5481 comp_896;

   @Inject(
      method = {"<init>(ILnet/minecraft/class_5481;Lnet/minecraft/class_7591;Z)V"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_303$class_7590;comp_896:Lnet/minecraft/class_5481;",
         opcode = 181,
         shift = Shift.AFTER
      )}
   )
   private void replace(int i, class_5481 orderedText, class_7591 messageIndicator, boolean bl, CallbackInfo ci) {
      this.comp_896 = new TextReplacementUtils.ReplacementOrderedText(this.comp_896);
   }
}
