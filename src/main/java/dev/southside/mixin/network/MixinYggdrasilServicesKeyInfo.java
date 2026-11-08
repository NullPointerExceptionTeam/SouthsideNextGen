package dev.southside.mixin.network;

import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   value = {YggdrasilServicesKeyInfo.class},
   remap = false
)
public class MixinYggdrasilServicesKeyInfo {
}
