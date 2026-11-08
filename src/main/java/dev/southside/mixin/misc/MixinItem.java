package dev.southside.mixin.misc;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_3959.class_242;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_1792.class})
public class MixinItem {
   @ModifyExpressionValue(
      method = {"method_7872(Lnet/minecraft/class_1937;Lnet/minecraft/class_1657;Lnet/minecraft/class_3959$class_242;)Lnet/minecraft/class_3965;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1657;method_36454()F"
      )}
   )
   private static float hookFixRotation(float original, class_1937 world, class_1657 player, class_242 fluidHandling) {
      if (player == class_310.method_1551().field_1724) {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      } else {
         return original;
      }
   }

   @ModifyExpressionValue(
      method = {"method_7872(Lnet/minecraft/class_1937;Lnet/minecraft/class_1657;Lnet/minecraft/class_3959$class_242;)Lnet/minecraft/class_3965;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1657;method_36455()F"
      )}
   )
   private static float hookFixRotationP(float original, class_1937 world, class_1657 player, class_242 fluidHandling) {
      if (player == class_310.method_1551().field_1724) {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().pitch : original;
      } else {
         return original;
      }
   }
}
