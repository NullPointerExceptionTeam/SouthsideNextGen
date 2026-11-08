package dev.southside.mixin.entity;

import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.module.impl.render.PlayerAppearance;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.class_1068;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_355;
import net.minecraft.class_640;
import net.minecraft.class_8685;
import net.minecraft.class_8685.class_7920;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_640.class})
public abstract class MixinPlayerListEntry {
   @Inject(
      method = {"method_52803(Lcom/mojang/authlib/GameProfile;)Ljava/util/function/Supplier;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onTexturesSupplier(GameProfile profile, CallbackInfoReturnable<Supplier<class_8685>> cir) {
      if (isHeypixel()) {
         class_8685 defaultSkin = class_1068.method_52854(profile);
         cir.setReturnValue((Supplier<class_8685>)() -> defaultSkin);
         cir.cancel();
      }
   }

   private static boolean isHeypixel() {
      if (class_310.method_1551().method_1542()) {
         return false;
      } else if (class_310.method_1551().field_1687 == null) {
         return false;
      } else {
         class_355 tabListHud = class_310.method_1551().field_1705.method_1750();
         if (tabListHud == null) {
            return false;
         } else {
            class_2561 header = tabListHud.field_2153;
            return header == null ? false : header.getString().toLowerCase(Locale.ROOT).contains("布吉岛");
         }
      }
   }

   @Inject(
      method = {"method_52810()Lnet/minecraft/class_8685;"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void getCapeTexture(CallbackInfoReturnable<class_8685> cir) {
      if (class_310.method_1551().field_1724 != null) {
         PlayerAppearance appearance = Southside.INSTANCE.getModuleManager().get(PlayerAppearance.class);
         if (appearance.isEnabled()) {
            class_640 self = (class_640)(Object)this;
            if (self.method_2966().getId().equals(class_310.method_1551().field_1724.method_7334().getId())) {
               if (appearance.getCustomCapeTexture() == null && appearance.getCustomCapeTexture() == null) {
                  return;
               }

               class_8685 prev = (class_8685)cir.getReturnValue();
               class_8685 newTextures = new class_8685(
                  appearance.setSkin.getValue() ? appearance.getCustomSkinTexture() : prev.comp_1626(),
                  prev.comp_1911(),
                  appearance.setCape.getValue() ? appearance.getCustomCapeTexture() : prev.comp_1627(),
                  appearance.setCape.getValue() ? appearance.getCustomCapeTexture() : prev.comp_1627(),
                  appearance.setSkin.getValue() ? class_7920.field_41122 : prev.comp_1629(),
                  prev.comp_1630()
               );
               cir.setReturnValue(newTextures);
            }
         }
      }
   }
}
