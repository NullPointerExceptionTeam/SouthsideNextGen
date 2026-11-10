package dev.southside.mixin.hud;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.gui.hud.ChatHud;
import dev.southside.module.impl.render.Chatting;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({net.minecraft.client.gui.hud.ChatHud.class})
public abstract class MixinChatHud {
   @Shadow
   public abstract int getWidth();

   @Inject(
      method = {"render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
      if (MinecraftAccessor.mc.world != null && Southside.INSTANCE.getModuleManager().get(Chatting.class).isEnabled()) {
         ci.cancel();
         ChatHud.render(context, currentTick, mouseX, mouseY);
      }
   }
}
