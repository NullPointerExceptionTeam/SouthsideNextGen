package dev.southside.mixin.misc;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.RaycastContext.FluidHandling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Item.class})
public class MixinItem {
   @ModifyExpressionValue(
      method = {"raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"
      )}
   )
   private static float hookFixRotation(float original, World world, PlayerEntity player, FluidHandling fluidHandling) {
      if (player == MinecraftClient.getInstance().player) {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      } else {
         return original;
      }
   }

   @ModifyExpressionValue(
      method = {"raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;getPitch()F"
      )}
   )
   private static float hookFixRotationP(float original, World world, PlayerEntity player, FluidHandling fluidHandling) {
      if (player == MinecraftClient.getInstance().player) {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().pitch : original;
      } else {
         return original;
      }
   }
}
