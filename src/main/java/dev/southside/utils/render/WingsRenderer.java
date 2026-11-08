package dev.southside.utils.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.PlayerAppearance;
import java.awt.Color;
import net.minecraft.class_10055;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_591;
import net.minecraft.class_7833;

public class WingsRenderer extends class_3887<class_10055, class_591> {
   private final WingsModel wingsModel = new WingsModel();
   private static final class_2960 WINGS_TEXTURE = class_2960.method_60655(Southside.CLIENT_NAME.toLowerCase(), "textures/wings/wings.png");

   public WingsRenderer(class_3883<class_10055, class_591> context) {
      super(context);
   }

   @Override
   public void method_4199(class_4587 matrices, class_4597 vertexConsumers, int light, class_10055 state, float limbAngle, float limbDistance) {
      this.render(matrices, vertexConsumers, light, state, limbAngle, limbDistance);
   }

   public void render(class_4587 matrices, class_4597 vertexConsumers, int light, class_10055 state, float limbAngle, float limbDistance) {
      PlayerAppearance appearance = Southside.INSTANCE.getModuleManager().get(PlayerAppearance.class);
      if (appearance.setWings.getValue()
         && appearance.isEnabled()
         && !state.field_53333
         && state.field_53528 == class_310.method_1551().field_1724.method_5628()) {
         matrices.method_22903();
         ((class_591)this.method_17165()).field_3391.method_22703(matrices);
         matrices.method_22904(0.0, 0.1F, 0.125);
         float scale = 1.2F;
         matrices.method_22905(scale, scale, scale);
         this.renderWings(matrices, vertexConsumers, light);
         matrices.method_22909();
      }
   }

   private void renderWings(class_4587 matrices, class_4597 vertexConsumers, int light) {
      Color color = Color.getHSBColor((float)(System.currentTimeMillis() % 1000L) / 1000.0F, 0.8F, 1.0F);
      class_4588 vertexConsumer = vertexConsumers.getBuffer(class_1921.method_23578(WINGS_TEXTURE));
      float time = (float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float) Math.PI * 2.0F;
      this.wingsModel.setAngles(time);
      matrices.method_22903();
      this.wingsModel.method_62100(matrices, vertexConsumer, light, class_4608.field_21444, color.getRGB());
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_22905(-1.0F, 1.0F, 1.0F);
      this.wingsModel.method_62100(matrices, vertexConsumer, light, class_4608.field_21444, color.getRGB());
      matrices.method_22909();
   }

   private void setupTransforms(class_4587 matrices, class_1657 player, float tickDelta) {
      double scale = 1.4;
      float bodyYaw = class_3532.method_17821(tickDelta, player.field_6220, player.field_6283);
      matrices.method_22905((float)(-scale), (float)(-scale), (float)scale);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F + bodyYaw));
      matrices.method_22904(0.0, -1.25 / scale, 0.0);
      matrices.method_22904(0.0, 0.0, 0.2 / scale);
      if (player.method_18276()) {
         matrices.method_22904(0.0, 0.125 / scale, 0.0);
      }
   }
}
