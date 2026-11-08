package dev.southside.mixin.gui;

import net.minecraft.class_4185;
import net.minecraft.class_8671;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_8671.class})
public class MixinReconfiguringScreen {
   @Shadow
   private class_4185 field_45510;

   @Inject(
      method = {"method_25426()V"},
      at = {@At("TAIL")}
   )
   private void forceEnableDisconnectOnInit(CallbackInfo ci) {
      if (this.field_45510 != null) {
         this.field_45510.field_22763 = true;
      }
   }

   @Inject(
      method = {"method_25393()V"},
      at = {@At("TAIL")}
   )
   private void keepDisconnectEnabled(CallbackInfo ci) {
      if (this.field_45510 != null && !this.field_45510.field_22763) {
         this.field_45510.field_22763 = true;
      }
   }
}
