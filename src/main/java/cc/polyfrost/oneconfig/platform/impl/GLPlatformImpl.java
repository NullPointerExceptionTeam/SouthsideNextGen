package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.platform.GLPlatform;
import dev.southside.render.RenderBuffer;
import dev.southside.render.RenderBuffers;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;

public class GLPlatformImpl implements GLPlatform {
   private final TextRenderer textRenderer = UMinecraft.getMinecraft().textRenderer;

   @Override
   public void drawRect(UMatrixStack matrixStack, float x, float y, float x2, float y2, int color) {
      Matrix4f matrix4f = matrixStack.peek().model();
      if (x < x2) {
         float i = x;
         x = x2;
         x2 = i;
      }

      if (y < y2) {
         float i = y;
         y = y2;
         y2 = i;
      }

      float alpha = ColorHelper.getAlpha(color) / 255.0F;
      float red = ColorHelper.getRed(color) / 255.0F;
      float green = ColorHelper.getGreen(color) / 255.0F;
      float blue = ColorHelper.getBlue(color) / 255.0F;
      VertexConsumer vertexConsumer = RenderBuffers.base.use(matrix4f).getVertexConsumer().getBuffer(RenderLayer.getGui());
      vertexConsumer.vertex(matrix4f, x, y, 0.0F).color(red, green, blue, alpha);
      vertexConsumer.vertex(matrix4f, x, y2, 0.0F).color(red, green, blue, alpha);
      vertexConsumer.vertex(matrix4f, x2, y2, 0.0F).color(red, green, blue, alpha);
      vertexConsumer.vertex(matrix4f, x2, y, 0.0F).color(red, green, blue, alpha);
   }

   @Override
   public void enableStencil(Runnable drawAction) {
      drawAction.run();
   }

   @Override
   public float drawText(UMatrixStack matrixStack, String text, float x, float y, int color, boolean shadow) {
      RenderBuffer buffer = RenderBuffers.text.use(matrixStack.peek().model());
      Immediate consumer = buffer.getVertexConsumer();
      return this.textRenderer.draw(text, x, y, color, shadow, buffer.getPositionMat(), consumer, TextLayerType.NORMAL, 0, 15728880);
   }

   @Override
   public int getStringWidth(String text) {
      return UMinecraft.getMinecraft().textRenderer.getWidth(text);
   }
}
