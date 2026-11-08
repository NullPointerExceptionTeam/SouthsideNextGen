package dev.southside.utils.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.module.impl.client.Theme;
import dev.southside.utils.misc.MSTimer;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_9801;
import net.minecraft.class_293.class_5596;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

public class MarkDrawer {
   private static class_310 mc = class_310.method_1551();
   private static float prevCircleStep;
   private static float circleStep;
   private static MSTimer timer = new MSTimer();
   private static float espValue = 1.0F;
   private static float prevEspValue;
   private static float espSpeed = 1.0F;
   private static boolean flipSpeed;

   public static void drawTargetEsp(class_4587 stack, @NotNull class_1297 target) {
      ArrayList<class_243> vecs = new ArrayList<>();
      ArrayList<class_243> vecs1 = new ArrayList<>();
      ArrayList<class_243> vecs2 = new ArrayList<>();
      double x = target.field_6014 + (target.method_23317() - target.field_6014) * getTickDelta() - mc.method_1561().field_4686.method_19326().method_10216();
      double y = target.field_6036 + (target.method_23318() - target.field_6036) * getTickDelta() - mc.method_1561().field_4686.method_19326().method_10214();
      double z = target.field_5969 + (target.method_23321() - target.field_5969) * getTickDelta() - mc.method_1561().field_4686.method_19326().method_10215();
      double height = target.method_17682();

      for (int i = 0; i <= 361; i++) {
         double v = Math.sin(Math.toRadians(i));
         double u = Math.cos(Math.toRadians(i));
         class_243 vec = new class_243((float)(u * 0.5), height, (float)(v * 0.5));
         vecs.add(vec);
         double v1 = Math.sin(Math.toRadians((i + 120) % 360));
         double u1 = Math.cos(Math.toRadians(i + 120) % 360.0);
         class_243 vec1 = new class_243((float)(u1 * 0.5), height, (float)(v1 * 0.5));
         vecs1.add(vec1);
         double v2 = Math.sin(Math.toRadians((i + 240) % 360));
         double u2 = Math.cos(Math.toRadians((i + 240) % 360));
         class_243 vec2 = new class_243((float)(u2 * 0.5), height, (float)(v2 * 0.5));
         vecs2.add(vec2);
         height -= 0.004F;
      }

      stack.method_22903();
      stack.method_22904(x, y, z);
      class_287 bufferBuilder = null;
      setupRender();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(class_10142.field_53876);
      Matrix4f matrix = stack.method_23760().method_23761();
      bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);

      for (int j = 0; j < vecs.size() - 1; j++) {
         float alpha = 1.0F - (j + (float)timer.getMS() / 5.0F) % 360.0F / 60.0F;
         bufferBuilder.method_22918(matrix, (float)vecs.get(j).field_1352, (float)vecs.get(j).field_1351, (float)vecs.get(j).field_1350)
            .method_39415(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
         bufferBuilder.method_22918(matrix, (float)vecs.get(j + 1).field_1352, (float)vecs.get(j + 1).field_1351 + 0.1F, (float)vecs.get(j + 1).field_1350)
            .method_39415(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
      }

      endBuilding(bufferBuilder);
      RenderSystem.setShader(class_10142.field_53876);
      bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);

      for (int j = 0; j < vecs1.size() - 1; j++) {
         float alpha = 1.0F - (j + (float)timer.getMS() / 5.0F) % 360.0F / 60.0F;
         bufferBuilder.method_22918(matrix, (float)vecs1.get(j).field_1352, (float)vecs1.get(j).field_1351, (float)vecs1.get(j).field_1350)
            .method_39415(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
         bufferBuilder.method_22918(matrix, (float)vecs1.get(j + 1).field_1352, (float)vecs1.get(j + 1).field_1351 + 0.1F, (float)vecs1.get(j + 1).field_1350)
            .method_39415(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
      }

      endBuilding(bufferBuilder);
      RenderSystem.setShader(class_10142.field_53876);
      bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);

      for (int j = 0; j < vecs2.size() - 1; j++) {
         float alpha = 1.0F - (j + (float)timer.getMS() / 5.0F) % 360.0F / 60.0F;
         bufferBuilder.method_22918(matrix, (float)vecs2.get(j).field_1352, (float)vecs2.get(j).field_1351, (float)vecs2.get(j).field_1350)
            .method_39415(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
         bufferBuilder.method_22918(matrix, (float)vecs2.get(j + 1).field_1352, (float)vecs2.get(j + 1).field_1351 + 0.1F, (float)vecs2.get(j + 1).field_1350)
            .method_39415(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
      }

      endBuilding(bufferBuilder);
      RenderSystem.enableCull();
      stack.method_22904(-x, -y, -z);
      endRender();
      RenderSystem.enableDepthTest();
      stack.method_22909();
   }

   public static void drawCircleEsp(class_4587 stack, class_1297 target) {
      class_4184 camera = mc.field_1773.method_19418();
      double cs = prevCircleStep + (circleStep - prevCircleStep) * getTickDelta();
      double prevSinAnim = absSinAnimation(cs - 0.45F);
      double sinAnim = absSinAnimation(cs);
      double x = target.field_6014 + (target.method_23317() - target.field_6014) * getTickDelta() - camera.method_19326().method_10216();
      double y = target.field_6036
         + (target.method_23318() - target.field_6036) * getTickDelta()
         - camera.method_19326().method_10214()
         + prevSinAnim * target.method_17682();
      double z = target.field_5969 + (target.method_23321() - target.field_5969) * getTickDelta() - camera.method_19326().method_10215();
      double nextY = target.field_6036
         + (target.method_23318() - target.field_6036) * getTickDelta()
         - camera.method_19326().method_10214()
         + sinAnim * target.method_17682();
      setupRender();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(class_10142.field_53876);
      Matrix4fStack modelView = RenderSystem.getModelViewStack();
      modelView.pushMatrix();
      modelView.mul(new Matrix4f().rotation(camera.method_23767().conjugate(new Quaternionf())));
      class_287 bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);

      for (int i = 0; i <= 30; i++) {
         float cos = (float)(x + Math.cos(i * 6.28 / 30.0) * target.method_17681() * 0.8);
         float sin = (float)(z + Math.sin(i * 6.28 / 30.0) * target.method_17681() * 0.8);
         bufferBuilder.method_22918(stack.method_23760().method_23761(), cos, (float)nextY, sin).method_39415(injectAlpha(Theme.getColor(i), 170).getRGB());
         bufferBuilder.method_22918(stack.method_23760().method_23761(), cos, (float)y, sin).method_39415(injectAlpha(Theme.getColor(i), 0).getRGB());
      }

      endBuilding(bufferBuilder);
      modelView.popMatrix();
      RenderSystem.enableCull();
      endRender();
      RenderSystem.enableDepthTest();
   }

   public static void renderTH(int espLength, int factor, float shaking, float amplitude, class_1297 target) {
      class_4184 camera = mc.field_1773.method_19418();
      double tPosX = interpolate(target.field_6014, target.method_23317(), getTickDelta()) - camera.method_19326().field_1352;
      double tPosY = interpolate(target.field_6036, target.method_23318(), getTickDelta()) - camera.method_19326().field_1351;
      double tPosZ = interpolate(target.field_5969, target.method_23321(), getTickDelta()) - camera.method_19326().field_1350;
      float iAge = (float)interpolate(target.field_6012 - 1, target.field_6012, getTickDelta());
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
      RenderSystem.setShaderTexture(0, class_2960.method_60655("southside", "hud/firefly.png"));
      RenderSystem.setShader(class_10142.field_53880);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      boolean canSee = mc.field_1724.method_6057(target);
      if (canSee) {
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
      } else {
         RenderSystem.disableDepthTest();
      }

      for (int j = 0; j < 3; j++) {
         for (int i = 0; i <= espLength; i++) {
            double radians = Math.toRadians(((i / 1.5F + iAge) * factor + j * 120) % (factor * 360));
            double sinQuad = Math.sin(Math.toRadians(iAge * 2.5F + i * (j + 1)) * amplitude) / shaking;
            float offset = (float)i / espLength;
            class_4587 matrices = new class_4587();
            matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(camera.method_19330() + 180.0F));
            matrices.method_22904(tPosX + Math.cos(radians) * target.method_17681(), tPosY + 1.0 + sinQuad, tPosZ + Math.sin(radians) * target.method_17681());
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
            Matrix4f matrix = matrices.method_23760().method_23761();
            int color = applyOpacity(Theme.getColor((int)(180.0F * offset)), offset).getRGB();
            float scale = Math.max(0.24F * offset, 0.2F);
            buffer.method_22918(matrix, -scale, scale, 0.0F).method_22913(0.0F, 1.0F).method_39415(color);
            buffer.method_22918(matrix, scale, scale, 0.0F).method_22913(1.0F, 1.0F).method_39415(color);
            buffer.method_22918(matrix, scale, -scale, 0.0F).method_22913(1.0F, 0.0F).method_39415(color);
            buffer.method_22918(matrix, -scale, -scale, 0.0F).method_22913(0.0F, 0.0F).method_39415(color);
         }
      }

      class_286.method_43433(buffer.method_60800());
      if (canSee) {
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
      } else {
         RenderSystem.enableDepthTest();
      }

      RenderSystem.disableBlend();
   }

   public static void renderNurikZapen(class_1297 target) {
      class_4184 camera = mc.field_1773.method_19418();
      double tPosX = interpolate(target.field_6014, target.method_23317(), getTickDelta()) - camera.method_19326().field_1352;
      double tPosY = interpolate(target.field_6036, target.method_23318(), getTickDelta()) - camera.method_19326().field_1351;
      double tPosZ = interpolate(target.field_5969, target.method_23321(), getTickDelta()) - camera.method_19326().field_1350;
      class_4587 matrices = new class_4587();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(camera.method_19330() + 180.0F));
      matrices.method_22904(tPosX, tPosY + target.method_18381(target.method_18376()) / 2.0F, tPosZ);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(interpolateFloat(prevEspValue, espValue, getTickDelta())));
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
      RenderSystem.setShaderTexture(0, class_2960.method_60655("southside", "hud/capture.png"));
      matrices.method_22904(-0.75, -0.75, -0.01);
      Matrix4f matrix = matrices.method_23760().method_23761();
      RenderSystem.setShader(class_10142.field_53880);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      buffer.method_22918(matrix, 0.0F, 1.5F, 0.0F).method_22913(0.0F, 1.0F).method_39415(Theme.getColor(90).getRGB());
      buffer.method_22918(matrix, 1.5F, 1.5F, 0.0F).method_22913(1.0F, 1.0F).method_39415(Theme.getColor(0).getRGB());
      buffer.method_22918(matrix, 1.5F, 0.0F, 0.0F).method_22913(1.0F, 0.0F).method_39415(Theme.getColor(180).getRGB());
      buffer.method_22918(matrix, 0.0F, 0.0F, 0.0F).method_22913(0.0F, 0.0F).method_39415(Theme.getColor(270).getRGB());
      class_286.method_43433(buffer.method_60800());
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static double absSinAnimation(double input) {
      return Math.abs(1.0 + Math.sin(input)) / 2.0;
   }

   private static void setupRender() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }

   private static void endRender() {
      RenderSystem.disableBlend();
   }

   private static Color injectAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), class_3532.method_15340(alpha, 0, 255));
   }

   private static void endBuilding(class_287 bb) {
      class_9801 builtBuffer = bb.method_60794();
      if (builtBuffer != null) {
         class_286.method_43433(builtBuffer);
      }
   }

   public static void tick() {
      prevCircleStep = circleStep;
      circleStep += 0.15F;
      prevEspValue = espValue;
      espValue = espValue + espSpeed;
      if (espSpeed > 25.0F) {
         flipSpeed = true;
      }

      if (espSpeed < -25.0F) {
         flipSpeed = false;
      }

      espSpeed = flipSpeed ? espSpeed - 0.5F : espSpeed + 0.5F;
   }

   private static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
      return (float)interpolate(oldValue, newValue, (float)interpolationValue);
   }

   private static double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   private static float getTickDelta() {
      return mc.method_61966().method_60637(false);
   }

   private static Color applyOpacity(Color color, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity));
   }

   static {
      timer.reset();
   }
}
