package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.accessor.EntityRenderStateAccessor;
import dev.southside.event.events.ApplyTransformationEvent;
import dev.southside.event.events.RenderItemEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.model.json.Transformation;

public class ItemPhysics extends Module {
   private final Switch randomRotation = new Switch("RandomRotation", true);
   private final Random random = Random.createLocal();
   private static final float PIXEL_SIZE = 0.0625F;
   private boolean renderingItem;

   public ItemPhysics() {
      super(Category.Render, "ItemPhysics");
   }

   @EventHandler
   private void onRenderItemEntity(RenderItemEntityEvent event) {
      ItemEntityRenderState state = event.renderState;
      MatrixStack matrices = event.matrixStack;
      VertexConsumerProvider vertexConsumers = event.vertexConsumerProvider;
      int light = event.light;
      ItemRenderState itemRenderState = state.itemRenderState;
      if (!itemRenderState.isEmpty()) {
         boolean flat = !itemRenderState.hasDepth();
         Transformation transformation = itemRenderState.getTransformation();
         matrices.push();

         try {
            this.random.setSeed(state.seed);
            this.applyGroundTransformation(matrices, transformation);
            float scaleY = transformation.scale.y();
            matrices.translate(0.0F, 0.25F * scaleY, 0.0F);
            this.offsetInWater(matrices, state);
            matrices.translate(0.0F, 0.001F, 0.0F);
            if (flat) {
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            }

            if (this.randomRotation.getValue()) {
               RotationAxis axis = flat ? RotationAxis.POSITIVE_Z : RotationAxis.POSITIVE_Y;
               float degrees = (this.random.nextFloat() * 2.0F - 1.0F) * 90.0F;
               matrices.multiply(axis.rotationDegrees(degrees));
            }

            this.renderingItem = true;
            int count = state.renderedAmount;

            for (int i = 0; i < count; i++) {
               matrices.push();
               if (i > 0) {
                  float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                  float z = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                  if (flat) {
                     matrices.translate(x, z, 0.0F);
                  } else {
                     matrices.translate(x, 0.0F, z);
                  }
               }

               itemRenderState.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
               matrices.pop();
               float stackOffset = Math.max(this.random.nextFloat() * 0.0625F, 0.03125F);
               if (flat) {
                  matrices.translate(0.0F, 0.0F, -stackOffset);
               } else {
                  matrices.translate(0.0F, stackOffset, 0.0F);
               }
            }

            this.renderingItem = false;
         } finally {
            this.renderingItem = false;
            matrices.pop();
         }

         event.cancel();
      }
   }

   @EventHandler
   private void onApplyTransformation(ApplyTransformationEvent event) {
      if (this.renderingItem) {
         event.cancel();
      }
   }

   private void applyGroundTransformation(MatrixStack matrices, Transformation transformation) {
      float prevY = transformation.translation.y;
      transformation.translation.y = 0.0F;
      transformation.apply(false, matrices);
      transformation.translation.y = prevY;
   }

   private void offsetInWater(MatrixStack matrices, ItemEntityRenderState state) {
      if (state instanceof EntityRenderStateAccessor accessor && accessor.southside$getEntity() instanceof ItemEntity itemEntity && itemEntity.isTouchingWater()) {
         matrices.translate(0.0F, 0.333F, 0.0F);
      }
   }
}
