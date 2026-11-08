package dev.southside.mixin.network;

import net.minecraft.class_2535;
import net.minecraft.class_310;
import net.minecraft.class_635;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({class_635.class})
public class MixinClientLoginNetworkHandler {
   @Shadow
   @Final
   private class_2535 field_3707;
   @Shadow
   @Final
   private class_310 field_3708;
}
