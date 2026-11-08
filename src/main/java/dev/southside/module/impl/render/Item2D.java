package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.RenderItemEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_10039;
import net.minecraft.class_10444;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;

public class Item2D extends Module {
   private final Slider scale = new Slider("Scale", 1.0F, 0.3F, 3.0F, 0.1F);
   private final Switch bobbing = new Switch("Bobbing", true);

   public Item2D() {
      super(Category.Render, "Item2D", "Render dropped items as flat 2D sprites facing the camera");
   }

   @EventHandler
   private void onRenderItemEntity(RenderItemEntityEvent event) {
      class_10039 state = event.renderState;
      class_4587 matrices = event.matrixStack;
      class_4597 vertexConsumers = event.vertexConsumerProvider;
      int light = event.light;
      class_10444 itemRenderState = state.field_55310;
      if (!itemRenderState.method_65606()) {
         class_4184 camera = mc.field_1773.method_19418();
         matrices.method_22903();
         float yOffset;
         if (this.bobbing.getValue()) {
            yOffset = class_3532.method_15374(state.field_53328 / 10.0F + state.field_53435) * 0.1F + 0.1F;
         } else {
            yOffset = 0.05F;
         }

         float scaleY = itemRenderState.method_65609().field_4285.y();
         matrices.method_46416(0.0F, yOffset + 0.25F * scaleY, 0.0F);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F - camera.method_19330()));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-camera.method_19329()));
         float s = this.scale.getValue().floatValue();
         matrices.method_22905(s, s, 0.01F);
         itemRenderState.method_65604(matrices, vertexConsumers, light, class_4608.field_21444);
         matrices.method_22909();
         event.cancel();
      }
   }
}
