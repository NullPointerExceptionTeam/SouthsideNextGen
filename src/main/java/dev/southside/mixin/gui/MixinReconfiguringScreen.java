package dev.southside.mixin.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ReconfiguringScreen.class})
public class MixinReconfiguringScreen {
   @Shadow
   private ButtonWidget disconnectButton;

   @Inject(
      method = {"init()V"},
      at = {@At("TAIL")}
   )
   private void forceEnableDisconnectOnInit(CallbackInfo ci) {
      if (this.disconnectButton != null) {
         this.disconnectButton.active = true;
      }
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("TAIL")}
   )
   private void keepDisconnectEnabled(CallbackInfo ci) {
      if (this.disconnectButton != null && !this.disconnectButton.active) {
         this.disconnectButton.active = true;
      }
   }
}
