package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.RenderItemEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.RotationAxis;

public class Item2D extends Module {
   private final Slider scale = new Slider("Scale", 1.0F, 0.3F, 3.0F, 0.1F);
   private final Switch bobbing = new Switch("Bobbing", true);

   public Item2D() {
      super(Category.Render, "Item2D", "Render dropped items as flat 2D sprites facing the camera");
   }

   @EventHandler
   private void onRenderItemEntity(RenderItemEntityEvent event) {
      ItemEntityRenderState state = event.renderState;
      MatrixStack matrices = event.matrixStack;
      VertexConsumerProvider vertexConsumers = event.vertexConsumerProvider;
      int light = event.light;
      ItemRenderState itemRenderState = state.itemRenderState;
      if (!itemRenderState.isEmpty()) {
         Camera camera = mc.gameRenderer.getCamera();
         matrices.push();
         float yOffset;
         if (this.bobbing.getValue()) {
            yOffset = MathHelper.sin(state.age / 10.0F + state.uniqueOffset) * 0.1F + 0.1F;
         } else {
            yOffset = 0.05F;
         }

         float scaleY = itemRenderState.getTransformation().scale.y();
         matrices.translate(0.0F, yOffset + 0.25F * scaleY, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - camera.getYaw()));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-camera.getPitch()));
         float s = this.scale.getValue().floatValue();
         matrices.scale(s, s, 0.01F);
         itemRenderState.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
         matrices.pop();
         event.cancel();
      }
   }
}
