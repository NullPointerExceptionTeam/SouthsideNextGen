package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.platform.GLPlatform;
import dev.southside.render.RenderBuffer;
import dev.southside.render.RenderBuffers;
import net.minecraft.class_1921;
import net.minecraft.class_327;
import net.minecraft.class_4588;
import net.minecraft.class_9848;
import net.minecraft.class_327.class_6415;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;

public class GLPlatformImpl implements GLPlatform {
   private final class_327 textRenderer = UMinecraft.getMinecraft().field_1772;

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

      float alpha = class_9848.method_61320(color) / 255.0F;
      float red = class_9848.method_61327(color) / 255.0F;
      float green = class_9848.method_61329(color) / 255.0F;
      float blue = class_9848.method_61331(color) / 255.0F;
      class_4588 vertexConsumer = RenderBuffers.base.use(matrix4f).getVertexConsumer().getBuffer(class_1921.method_51784());
      vertexConsumer.method_22918(matrix4f, x, y, 0.0F).method_22915(red, green, blue, alpha);
      vertexConsumer.method_22918(matrix4f, x, y2, 0.0F).method_22915(red, green, blue, alpha);
      vertexConsumer.method_22918(matrix4f, x2, y2, 0.0F).method_22915(red, green, blue, alpha);
      vertexConsumer.method_22918(matrix4f, x2, y, 0.0F).method_22915(red, green, blue, alpha);
   }

   @Override
   public void enableStencil(Runnable drawAction) {
      drawAction.run();
   }

   @Override
   public float drawText(UMatrixStack matrixStack, String text, float x, float y, int color, boolean shadow) {
      RenderBuffer buffer = RenderBuffers.text.use(matrixStack.peek().model());
      class_4598 consumer = buffer.getVertexConsumer();
      return this.textRenderer.method_27521(text, x, y, color, shadow, buffer.getPositionMat(), consumer, class_6415.field_33993, 0, 15728880);
   }

   @Override
   public int getStringWidth(String text) {
      return UMinecraft.getMinecraft().field_1772.method_1727(text);
   }
}
