package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.accessor.EntityRenderStateAccessor;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.render.LineBreakFormattor;
import dev.southside.module.impl.render.NameTag;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {
   @Shadow
   @Final
   protected EntityRenderDispatcher dispatcher;

   @Inject(
      method = {"updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void attachRenderStateEntity(T entity, S state, float tickDelta, CallbackInfo ci) {
      if (state instanceof EntityRenderStateAccessor accessor) {
         accessor.southside$setEntity(entity);
      }
   }

   @Inject(
      method = {"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderLabelIfPresent(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      ModuleManager moduleManager = Southside.get().getModuleManager();
      Entity entity = null;
      if (state instanceof EntityRenderStateAccessor accessor) {
         entity = accessor.southside$getEntity();
      }

      NoRender noRender = moduleManager.get(NoRender.class);
      if (entity instanceof ArmorStandEntity && noRender != null && noRender.isEnabled() && noRender.noArmorStands.getValue()) {
         ci.cancel();
      } else {
         NameTag nameTag = moduleManager.get(NameTag.class);
         if (nameTag != null && nameTag.isEnabled() && nameTag.hideVanilla.getValue() && entity instanceof PlayerEntity) {
            ci.cancel();
         } else {
            LineBreakFormattor lineBreakFormattor = moduleManager.get(LineBreakFormattor.class);
            if (lineBreakFormattor != null && lineBreakFormattor.renderMultilineLabel(state, text, matrices, vertexConsumers, light)) {
               ci.cancel();
            }
         }
      }
   }
}
