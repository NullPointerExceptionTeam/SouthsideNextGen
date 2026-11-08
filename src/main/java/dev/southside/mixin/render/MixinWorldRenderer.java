package dev.southside.mixin.render;

import net.minecraft.class_310;
import net.minecraft.class_4066;
import net.minecraft.class_638;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({class_761.class})
public class MixinWorldRenderer {
   @Final
   @Shadow
   private class_310 field_4088;
   @Shadow
   private class_638 field_4085;

   @Overwrite
   private class_4066 method_3268(boolean canSpawnOnMinimal) {
      if (this.field_4088 != null && this.field_4085 != null) {
         class_4066 particlesMode = (class_4066)this.field_4088.field_1690.method_42475().method_41753();
         if (canSpawnOnMinimal && particlesMode == class_4066.field_18199 && this.field_4085.field_9229.method_43048(10) == 0) {
            particlesMode = class_4066.field_18198;
         }

         if (particlesMode == class_4066.field_18198 && this.field_4085.field_9229.method_43048(3) == 0) {
            particlesMode = class_4066.field_18199;
         }

         return particlesMode;
      } else {
         return class_4066.field_18199;
      }
   }
}
