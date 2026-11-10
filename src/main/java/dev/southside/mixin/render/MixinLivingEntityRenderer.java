package dev.southside.mixin.render;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {
   @Unique
   private boolean isPlayer = false;

   protected MixinLivingEntityRenderer(Context context) {
      super(context);
   }

   @Inject(
      method = {"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void inject(T entity, S state, float delta, CallbackInfo ci) {
      this.isPlayer = MinecraftAccessor.mc.player != null && entity == MinecraftAccessor.mc.player;
      if (this.isPlayer) {
         RotationUtils.setBodyYaw((PlayerEntityRenderState)state, delta);
         RotationUtils.setHeadYaw((PlayerEntityRenderState)state, delta);
         RotationUtils.setPitch((PlayerEntityRenderState)state, delta);
      }
   }
}
