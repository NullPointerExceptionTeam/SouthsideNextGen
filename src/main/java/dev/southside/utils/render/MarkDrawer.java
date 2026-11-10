package dev.southside.utils.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.module.impl.client.Theme;
import dev.southside.utils.misc.MSTimer;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

public class MarkDrawer {
   private static MinecraftClient mc = MinecraftClient.getInstance();
   private static float prevCircleStep;
   private static float circleStep;
   private static MSTimer timer = new MSTimer();
   private static float espValue = 1.0F;
   private static float prevEspValue;
   private static float espSpeed = 1.0F;
   private static boolean flipSpeed;

   public static void drawTargetEsp(MatrixStack stack, @NotNull Entity target) {
      ArrayList<Vec3d> vecs = new ArrayList<>();
      ArrayList<Vec3d> vecs1 = new ArrayList<>();
      ArrayList<Vec3d> vecs2 = new ArrayList<>();
      double x = target.prevX + (target.getX() - target.prevX) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = target.prevY + (target.getY() - target.prevY) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
      double z = target.prevZ + (target.getZ() - target.prevZ) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
      double height = target.getHeight();

      for (int i = 0; i <= 361; i++) {
         double v = Math.sin(Math.toRadians(i));
         double u = Math.cos(Math.toRadians(i));
         Vec3d vec = new Vec3d((float)(u * 0.5), height, (float)(v * 0.5));
         vecs.add(vec);
         double v1 = Math.sin(Math.toRadians((i + 120) % 360));
         double u1 = Math.cos(Math.toRadians(i + 120) % 360.0);
         Vec3d vec1 = new Vec3d((float)(u1 * 0.5), height, (float)(v1 * 0.5));
         vecs1.add(vec1);
         double v2 = Math.sin(Math.toRadians((i + 240) % 360));
         double u2 = Math.cos(Math.toRadians((i + 240) % 360));
         Vec3d vec2 = new Vec3d((float)(u2 * 0.5), height, (float)(v2 * 0.5));
         vecs2.add(vec2);
         height -= 0.004F;
      }

      stack.push();
      stack.translate(x, y, z);
      BufferBuilder bufferBuilder = null;
      setupRender();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      Matrix4f matrix = stack.peek().getPositionMatrix();
      bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

      for (int j = 0; j < vecs.size() - 1; j++) {
         float alpha = 1.0F - (j + (float)timer.getMS() / 5.0F) % 360.0F / 60.0F;
         bufferBuilder.vertex(matrix, (float)vecs.get(j).x, (float)vecs.get(j).y, (float)vecs.get(j).z)
            .color(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
         bufferBuilder.vertex(matrix, (float)vecs.get(j + 1).x, (float)vecs.get(j + 1).y + 0.1F, (float)vecs.get(j + 1).z)
            .color(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
      }

      endBuilding(bufferBuilder);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

      for (int j = 0; j < vecs1.size() - 1; j++) {
         float alpha = 1.0F - (j + (float)timer.getMS() / 5.0F) % 360.0F / 60.0F;
         bufferBuilder.vertex(matrix, (float)vecs1.get(j).x, (float)vecs1.get(j).y, (float)vecs1.get(j).z)
            .color(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
         bufferBuilder.vertex(matrix, (float)vecs1.get(j + 1).x, (float)vecs1.get(j + 1).y + 0.1F, (float)vecs1.get(j + 1).z)
            .color(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
      }

      endBuilding(bufferBuilder);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

      for (int j = 0; j < vecs2.size() - 1; j++) {
         float alpha = 1.0F - (j + (float)timer.getMS() / 5.0F) % 360.0F / 60.0F;
         bufferBuilder.vertex(matrix, (float)vecs2.get(j).x, (float)vecs2.get(j).y, (float)vecs2.get(j).z)
            .color(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
         bufferBuilder.vertex(matrix, (float)vecs2.get(j + 1).x, (float)vecs2.get(j + 1).y + 0.1F, (float)vecs2.get(j + 1).z)
            .color(injectAlpha(Theme.getColor((int)(j / 20.0F)), (int)(alpha * 255.0F)).getRGB());
      }

      endBuilding(bufferBuilder);
      RenderSystem.enableCull();
      stack.translate(-x, -y, -z);
      endRender();
      RenderSystem.enableDepthTest();
      stack.pop();
   }

   public static void drawCircleEsp(MatrixStack stack, Entity target) {
      Camera camera = mc.gameRenderer.getCamera();
      double cs = prevCircleStep + (circleStep - prevCircleStep) * getTickDelta();
      double prevSinAnim = absSinAnimation(cs - 0.45F);
      double sinAnim = absSinAnimation(cs);
      double x = target.prevX + (target.getX() - target.prevX) * getTickDelta() - camera.getPos().getX();
      double y = target.prevY
         + (target.getY() - target.prevY) * getTickDelta()
         - camera.getPos().getY()
         + prevSinAnim * target.getHeight();
      double z = target.prevZ + (target.getZ() - target.prevZ) * getTickDelta() - camera.getPos().getZ();
      double nextY = target.prevY
         + (target.getY() - target.prevY) * getTickDelta()
         - camera.getPos().getY()
         + sinAnim * target.getHeight();
      setupRender();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      Matrix4fStack modelView = RenderSystem.getModelViewStack();
      modelView.pushMatrix();
      modelView.mul(new Matrix4f().rotation(camera.getRotation().conjugate(new Quaternionf())));
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

      for (int i = 0; i <= 30; i++) {
         float cos = (float)(x + Math.cos(i * 6.28 / 30.0) * target.getWidth() * 0.8);
         float sin = (float)(z + Math.sin(i * 6.28 / 30.0) * target.getWidth() * 0.8);
         bufferBuilder.vertex(stack.peek().getPositionMatrix(), cos, (float)nextY, sin).color(injectAlpha(Theme.getColor(i), 170).getRGB());
         bufferBuilder.vertex(stack.peek().getPositionMatrix(), cos, (float)y, sin).color(injectAlpha(Theme.getColor(i), 0).getRGB());
      }

      endBuilding(bufferBuilder);
      modelView.popMatrix();
      RenderSystem.enableCull();
      endRender();
      RenderSystem.enableDepthTest();
   }

   public static void renderTH(int espLength, int factor, float shaking, float amplitude, Entity target) {
      Camera camera = mc.gameRenderer.getCamera();
      double tPosX = interpolate(target.prevX, target.getX(), getTickDelta()) - camera.getPos().x;
      double tPosY = interpolate(target.prevY, target.getY(), getTickDelta()) - camera.getPos().y;
      double tPosZ = interpolate(target.prevZ, target.getZ(), getTickDelta()) - camera.getPos().z;
      float iAge = (float)interpolate(target.age - 1, target.age, getTickDelta());
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, Identifier.of("southside", "hud/firefly.png"));
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      boolean canSee = mc.player.canSee(target);
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
            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(tPosX + Math.cos(radians) * target.getWidth(), tPosY + 1.0 + sinQuad, tPosZ + Math.sin(radians) * target.getWidth());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            int color = applyOpacity(Theme.getColor((int)(180.0F * offset)), offset).getRGB();
            float scale = Math.max(0.24F * offset, 0.2F);
            buffer.vertex(matrix, -scale, scale, 0.0F).texture(0.0F, 1.0F).color(color);
            buffer.vertex(matrix, scale, scale, 0.0F).texture(1.0F, 1.0F).color(color);
            buffer.vertex(matrix, scale, -scale, 0.0F).texture(1.0F, 0.0F).color(color);
            buffer.vertex(matrix, -scale, -scale, 0.0F).texture(0.0F, 0.0F).color(color);
         }
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      if (canSee) {
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
      } else {
         RenderSystem.enableDepthTest();
      }

      RenderSystem.disableBlend();
   }

   public static void renderNurikZapen(Entity target) {
      Camera camera = mc.gameRenderer.getCamera();
      double tPosX = interpolate(target.prevX, target.getX(), getTickDelta()) - camera.getPos().x;
      double tPosY = interpolate(target.prevY, target.getY(), getTickDelta()) - camera.getPos().y;
      double tPosZ = interpolate(target.prevZ, target.getZ(), getTickDelta()) - camera.getPos().z;
      MatrixStack matrices = new MatrixStack();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
      matrices.translate(tPosX, tPosY + target.getEyeHeight(target.getPose()) / 2.0F, tPosZ);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(interpolateFloat(prevEspValue, espValue, getTickDelta())));
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, Identifier.of("southside", "hud/capture.png"));
      matrices.translate(-0.75, -0.75, -0.01);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(matrix, 0.0F, 1.5F, 0.0F).texture(0.0F, 1.0F).color(Theme.getColor(90).getRGB());
      buffer.vertex(matrix, 1.5F, 1.5F, 0.0F).texture(1.0F, 1.0F).color(Theme.getColor(0).getRGB());
      buffer.vertex(matrix, 1.5F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(Theme.getColor(180).getRGB());
      buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(Theme.getColor(270).getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
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
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
   }

   private static void endBuilding(BufferBuilder bb) {
      BuiltBuffer builtBuffer = bb.endNullable();
      if (builtBuffer != null) {
         BufferRenderer.drawWithGlobalProgram(builtBuffer);
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
      return mc.getRenderTickCounter().getTickDelta(false);
   }

   private static Color applyOpacity(Color color, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity));
   }

   static {
      timer.reset();
   }
}
