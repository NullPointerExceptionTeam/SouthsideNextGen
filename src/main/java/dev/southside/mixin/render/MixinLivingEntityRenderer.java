package dev.southside.mixin.render;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1309;
import net.minecraft.class_583;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_922.class})
public abstract class MixinLivingEntityRenderer<T extends class_1309, S extends class_10042, M extends class_583<? super S>> extends class_897<T, S> {
   @Unique
   private boolean isPlayer = false;

   protected MixinLivingEntityRenderer(class_5618 context) {
      super(context);
   }

   @Inject(
      method = {"method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V"},
      at = {@At("TAIL")}
   )
   private void inject(T entity, S state, float delta, CallbackInfo ci) {
      this.isPlayer = MinecraftAccessor.mc.field_1724 != null && entity == MinecraftAccessor.mc.field_1724;
      if (this.isPlayer) {
         RotationUtils.setBodyYaw((class_10055)state, delta);
         RotationUtils.setHeadYaw((class_10055)state, delta);
         RotationUtils.setPitch((class_10055)state, delta);
      }
   }
}
