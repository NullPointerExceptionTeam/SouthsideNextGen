package dev.southside.module.impl.render;

import dev.southside.module.Category;
import dev.southside.module.Module;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import org.joml.Matrix4f;

public class LineBreakFormattor extends Module {
   public LineBreakFormattor() {
      super(Category.Render, "LineBreakFormattor", "Render nametags that contain literal \\n on multiple lines.");
   }

   public boolean renderMultilineLabel(EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (this.isEnabled() && text != null && state != null) {
         EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
         if (dispatcher == null || mc.textRenderer == null || mc.options == null) {
            return false;
         } else if (state.squaredDistanceToCamera > 4096.0) {
            return false;
         } else {
            Vec3d labelPos = state.nameLabelPos;
            if (labelPos == null) {
               return false;
            } else {
               LineBreakFormattor.SplitResult splitResult = this.splitOrderedText(text.asOrderedText());
               if (!splitResult.hasLineBreaks()) {
                  return false;
               } else {
                  List<OrderedText> lines = splitResult.lines();
                  boolean sneaking = state.sneaking;
                  TextRenderer textRenderer = mc.textRenderer;
                  float baseYOffset = "deadmau5".equals(text.getString()) ? -10.0F : 0.0F;
                  float lineSpacing = 9.0F + 2.0F;
                  float yOffset = baseYOffset - (lines.size() - 1) * lineSpacing / 2.0F;
                  matrices.push();
                  matrices.translate(labelPos.x, labelPos.y + 0.5, labelPos.z);
                  matrices.multiply(dispatcher.getRotation());
                  matrices.scale(0.025F, -0.025F, 0.025F);
                  Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                  float backgroundOpacity = mc.options.getTextBackgroundOpacity(0.25F);
                  int backgroundColor = (int)(backgroundOpacity * 255.0F) << 24;

                  for (OrderedText line : lines) {
                     this.renderSingleLine(textRenderer, matrix4f, vertexConsumers, line, sneaking, backgroundColor, light, yOffset);
                     yOffset += lineSpacing;
                  }

                  matrices.pop();
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private void renderSingleLine(
      TextRenderer textRenderer, Matrix4f matrix, VertexConsumerProvider vertexConsumers, OrderedText line, boolean sneaking, int backgroundColor, int light, float y
   ) {
      float x = -textRenderer.getWidth(line) / 2.0F;
      TextLayerType layer = sneaking ? TextLayerType.SEE_THROUGH : TextLayerType.NORMAL;
      textRenderer.draw(line, x, y, 553648127, false, matrix, vertexConsumers, layer, backgroundColor, light);
      if (!sneaking) {
         textRenderer.draw(line, x, y, -1, false, matrix, vertexConsumers, TextLayerType.NORMAL, 0, light);
      }
   }

   private LineBreakFormattor.SplitResult splitOrderedText(OrderedText orderedText) {
      List<OrderedText> lines = new ArrayList<>();
      List<LineBreakFormattor.MappedCharacter> current = new ArrayList<>();
      LineBreakFormattor.MappedCharacter[] pendingBackslash = new LineBreakFormattor.MappedCharacter[1];
      boolean[] foundBreak = new boolean[1];
      orderedText.accept((__, style, codePoint) -> {
         if (pendingBackslash[0] != null) {
            if (codePoint == 110) {
               lines.add(this.createOrderedText(current));
               current.clear();
               pendingBackslash[0] = null;
               foundBreak[0] = true;
               return true;
            }

            current.add(pendingBackslash[0]);
            pendingBackslash[0] = null;
         }

         if (codePoint == 92) {
            pendingBackslash[0] = new LineBreakFormattor.MappedCharacter(style, codePoint);
            return true;
         } else {
            current.add(new LineBreakFormattor.MappedCharacter(style, codePoint));
            return true;
         }
      });
      if (pendingBackslash[0] != null) {
         current.add(pendingBackslash[0]);
      }

      lines.add(this.createOrderedText(current));
      return new LineBreakFormattor.SplitResult(lines, foundBreak[0]);
   }

   private OrderedText createOrderedText(List<LineBreakFormattor.MappedCharacter> chars) {
      List<LineBreakFormattor.MappedCharacter> snapshot = List.copyOf(chars);
      return visitor -> {
         int index = 0;

         for (LineBreakFormattor.MappedCharacter mapped : snapshot) {
            if (!visitor.accept(index++, mapped.style, mapped.codePoint)) {
               return false;
            }
         }

         return true;
      };
   }

   private record MappedCharacter(Style style, int codePoint) {
   }

   private record SplitResult(List<OrderedText> lines, boolean hasLineBreaks) {
   }
}
