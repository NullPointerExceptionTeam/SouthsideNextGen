package dev.southside.mixin.entity;

import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.module.impl.render.PlayerAppearance;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.SkinTextures.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListEntry.class})
public abstract class MixinPlayerListEntry {
   @Inject(
      method = {"texturesSupplier(Lcom/mojang/authlib/GameProfile;)Ljava/util/function/Supplier;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onTexturesSupplier(GameProfile profile, CallbackInfoReturnable<Supplier<SkinTextures>> cir) {
      if (isHeypixel()) {
         SkinTextures defaultSkin = DefaultSkinHelper.getSkinTextures(profile);
         cir.setReturnValue((Supplier<SkinTextures>)() -> defaultSkin);
         cir.cancel();
      }
   }

   private static boolean isHeypixel() {
      if (MinecraftClient.getInstance().isInSingleplayer()) {
         return false;
      } else if (MinecraftClient.getInstance().world == null) {
         return false;
      } else {
         PlayerListHud tabListHud = MinecraftClient.getInstance().inGameHud.getPlayerListHud();
         if (tabListHud == null) {
            return false;
         } else {
            Text header = tabListHud.header;
            return header == null ? false : header.getString().toLowerCase(Locale.ROOT).contains("布吉岛");
         }
      }
   }

   @Inject(
      method = {"getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void getCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
      if (MinecraftClient.getInstance().player != null) {
         PlayerAppearance appearance = Southside.INSTANCE.getModuleManager().get(PlayerAppearance.class);
         if (appearance.isEnabled()) {
            PlayerListEntry self = (PlayerListEntry)(Object)this;
            if (self.getProfile().getId().equals(MinecraftClient.getInstance().player.getGameProfile().getId())) {
               if (appearance.getCustomCapeTexture() == null && appearance.getCustomCapeTexture() == null) {
                  return;
               }

               SkinTextures prev = (SkinTextures)cir.getReturnValue();
               SkinTextures newTextures = new SkinTextures(
                  appearance.setSkin.getValue() ? appearance.getCustomSkinTexture() : prev.texture(),
                  prev.textureUrl(),
                  appearance.setCape.getValue() ? appearance.getCustomCapeTexture() : prev.capeTexture(),
                  appearance.setCape.getValue() ? appearance.getCustomCapeTexture() : prev.capeTexture(),
                  appearance.setSkin.getValue() ? Model.SLIM : prev.model(),
                  prev.secure()
               );
               cir.setReturnValue(newTextures);
            }
         }
      }
   }
}
