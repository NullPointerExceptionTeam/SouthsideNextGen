package dev.southside.mixin.hud;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.gui.hud.ChatHud;
import dev.southside.module.impl.render.Chatting;
import net.minecraft.class_332;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_338.class})
public abstract class MixinChatHud {
   @Shadow
   public abstract int method_1811();

   @Inject(
      method = {"method_1805(Lnet/minecraft/class_332;IIIZ)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void render(class_332 context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
      if (MinecraftAccessor.mc.field_1687 != null && Southside.INSTANCE.getModuleManager().get(Chatting.class).isEnabled()) {
         ci.cancel();
         ChatHud.render(context, currentTick, mouseX, mouseY);
      }
   }
}
