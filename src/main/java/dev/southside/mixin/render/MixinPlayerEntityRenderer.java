package dev.southside.mixin.render;

import dev.southside.utils.render.WingsRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntityRenderer.class})
public class MixinPlayerEntityRenderer {
   @Inject(
      method = {"<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)V"},
      at = {@At("RETURN")}
   )
   private void addWingsLayer(Context ctx, boolean slim, CallbackInfo ci) {
      PlayerEntityRenderer renderer = (PlayerEntityRenderer)(Object)this;
      renderer.addFeature(new WingsRenderer((FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>)this));
   }
}
