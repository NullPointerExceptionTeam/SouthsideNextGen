package dev.southside.mixin.block;

import dev.southside.Southside;
import dev.southside.event.events.EntityCobwebCollisionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CobwebBlock.class})
public class MixinCobwebBlock {
   @Inject(
      method = {"onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
      EntityCobwebCollisionEvent event = new EntityCobwebCollisionEvent(state, world, pos, entity);
      Southside.get().getEventBus().post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }
}
