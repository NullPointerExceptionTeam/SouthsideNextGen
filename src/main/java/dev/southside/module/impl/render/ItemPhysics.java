package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.accessor.EntityRenderStateAccessor;
import dev.southside.event.events.ApplyTransformationEvent;
import dev.southside.event.events.RenderItemEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_10039;
import net.minecraft.class_10444;
import net.minecraft.class_1542;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5819;
import net.minecraft.class_7833;
import net.minecraft.class_804;

public class ItemPhysics extends Module {
   private final Switch randomRotation = new Switch("RandomRotation", true);
   private final class_5819 random = class_5819.method_43053();
   private static final float PIXEL_SIZE = 0.0625F;
   private boolean renderingItem;

   public ItemPhysics() {
      super(Category.Render, "ItemPhysics");
   }

   @EventHandler
   private void onRenderItemEntity(RenderItemEntityEvent event) {
      class_10039 state = event.renderState;
      class_4587 matrices = event.matrixStack;
      class_4597 vertexConsumers = event.vertexConsumerProvider;
      int light = event.light;
      class_10444 itemRenderState = state.field_55310;
      if (!itemRenderState.method_65606()) {
         boolean flat = !itemRenderState.method_65607();
         class_804 transformation = itemRenderState.method_65609();
         matrices.method_22903();

         try {
            this.random.method_43052(state.field_55312);
            this.applyGroundTransformation(matrices, transformation);
            float scaleY = transformation.field_4285.y();
            matrices.method_46416(0.0F, 0.25F * scaleY, 0.0F);
            this.offsetInWater(matrices, state);
            matrices.method_46416(0.0F, 0.001F, 0.0F);
            if (flat) {
               matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
            }

            if (this.randomRotation.getValue()) {
               class_7833 axis = flat ? class_7833.field_40718 : class_7833.field_40716;
               float degrees = (this.random.method_43057() * 2.0F - 1.0F) * 90.0F;
               matrices.method_22907(axis.rotationDegrees(degrees));
            }

            this.renderingItem = true;
            int count = state.field_55311;

            for (int i = 0; i < count; i++) {
               matrices.method_22903();
               if (i > 0) {
                  float x = (this.random.method_43057() * 2.0F - 1.0F) * 0.15F;
                  float z = (this.random.method_43057() * 2.0F - 1.0F) * 0.15F;
                  if (flat) {
                     matrices.method_46416(x, z, 0.0F);
                  } else {
                     matrices.method_46416(x, 0.0F, z);
                  }
               }

               itemRenderState.method_65604(matrices, vertexConsumers, light, class_4608.field_21444);
               matrices.method_22909();
               float stackOffset = Math.max(this.random.method_43057() * 0.0625F, 0.03125F);
               if (flat) {
                  matrices.method_46416(0.0F, 0.0F, -stackOffset);
               } else {
                  matrices.method_46416(0.0F, stackOffset, 0.0F);
               }
            }

            this.renderingItem = false;
         } finally {
            this.renderingItem = false;
            matrices.method_22909();
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

   private void applyGroundTransformation(class_4587 matrices, class_804 transformation) {
      float prevY = transformation.field_4286.y;
      transformation.field_4286.y = 0.0F;
      transformation.method_23075(false, matrices);
      transformation.field_4286.y = prevY;
   }

   private void offsetInWater(class_4587 matrices, class_10039 state) {
      if (state instanceof EntityRenderStateAccessor accessor && accessor.southside$getEntity() instanceof class_1542 itemEntity && itemEntity.method_5799()) {
         matrices.method_46416(0.0F, 0.333F, 0.0F);
      }
   }
}
