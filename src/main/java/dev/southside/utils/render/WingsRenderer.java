package dev.southside.utils.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.PlayerAppearance;
import java.awt.Color;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.RotationAxis;

public class WingsRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   private final WingsModel wingsModel = new WingsModel();
   private static final Identifier WINGS_TEXTURE = Identifier.of(Southside.CLIENT_NAME.toLowerCase(), "textures/wings/wings.png");

   public WingsRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
      super(context);
   }

   @Override
   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
      this.renderWingsFor(matrices, vertexConsumers, light, state, limbAngle, limbDistance);
   }

   public void renderWingsFor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
      PlayerAppearance appearance = Southside.INSTANCE.getModuleManager().get(PlayerAppearance.class);
      if (appearance.setWings.getValue()
         && appearance.isEnabled()
         && !state.invisible
         && state.id == MinecraftClient.getInstance().player.getId()) {
         matrices.push();
         ((PlayerEntityModel)this.getContextModel()).body.rotate(matrices);
         matrices.translate(0.0, 0.1F, 0.125);
         float scale = 1.2F;
         matrices.scale(scale, scale, scale);
         this.renderWings(matrices, vertexConsumers, light);
         matrices.pop();
      }
   }

   private void renderWings(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      Color color = Color.getHSBColor((float)(System.currentTimeMillis() % 1000L) / 1000.0F, 0.8F, 1.0F);
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(WINGS_TEXTURE));
      float time = (float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float) Math.PI * 2.0F;
      this.wingsModel.setAngles(time);
      matrices.push();
      this.wingsModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color.getRGB());
      matrices.pop();
      matrices.push();
      matrices.scale(-1.0F, 1.0F, 1.0F);
      this.wingsModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color.getRGB());
      matrices.pop();
   }

   private void setupTransforms(MatrixStack matrices, PlayerEntity player, float tickDelta) {
      double scale = 1.4;
      float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
      matrices.scale((float)(-scale), (float)(-scale), (float)scale);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F + bodyYaw));
      matrices.translate(0.0, -1.25 / scale, 0.0);
      matrices.translate(0.0, 0.0, 0.2 / scale);
      if (player.isInSneakingPose()) {
         matrices.translate(0.0, 0.125 / scale, 0.0);
      }
   }
}
