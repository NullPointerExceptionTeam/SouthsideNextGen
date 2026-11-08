package dev.southside.module.impl.render;

import dev.southside.module.Category;
import dev.southside.module.Module;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10017;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5481;
import net.minecraft.class_898;
import net.minecraft.class_327.class_6415;
import org.joml.Matrix4f;

public class LineBreakFormattor extends Module {
   public LineBreakFormattor() {
      super(Category.Render, "LineBreakFormattor", "Render nametags that contain literal \\n on multiple lines.");
   }

   public boolean renderMultilineLabel(class_10017 state, class_2561 text, class_4587 matrices, class_4597 vertexConsumers, int light) {
      if (this.isEnabled() && text != null && state != null) {
         class_898 dispatcher = mc.method_1561();
         if (dispatcher == null || mc.field_1772 == null || mc.field_1690 == null) {
            return false;
         } else if (state.field_53332 > 4096.0) {
            return false;
         } else {
            class_243 labelPos = state.field_53338;
            if (labelPos == null) {
               return false;
            } else {
               LineBreakFormattor.SplitResult splitResult = this.splitOrderedText(text.method_30937());
               if (!splitResult.hasLineBreaks()) {
                  return false;
               } else {
                  List<class_5481> lines = splitResult.lines();
                  boolean sneaking = state.field_53334;
                  class_327 textRenderer = mc.field_1772;
                  float baseYOffset = "deadmau5".equals(text.getString()) ? -10.0F : 0.0F;
                  float lineSpacing = 9.0F + 2.0F;
                  float yOffset = baseYOffset - (lines.size() - 1) * lineSpacing / 2.0F;
                  matrices.method_22903();
                  matrices.method_22904(labelPos.field_1352, labelPos.field_1351 + 0.5, labelPos.field_1350);
                  matrices.method_22907(dispatcher.method_24197());
                  matrices.method_22905(0.025F, -0.025F, 0.025F);
                  Matrix4f matrix4f = matrices.method_23760().method_23761();
                  float backgroundOpacity = mc.field_1690.method_19343(0.25F);
                  int backgroundColor = (int)(backgroundOpacity * 255.0F) << 24;

                  for (class_5481 line : lines) {
                     this.renderSingleLine(textRenderer, matrix4f, vertexConsumers, line, sneaking, backgroundColor, light, yOffset);
                     yOffset += lineSpacing;
                  }

                  matrices.method_22909();
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private void renderSingleLine(
      class_327 textRenderer, Matrix4f matrix, class_4597 vertexConsumers, class_5481 line, boolean sneaking, int backgroundColor, int light, float y
   ) {
      float x = -textRenderer.method_30880(line) / 2.0F;
      class_6415 layer = sneaking ? class_6415.field_33994 : class_6415.field_33993;
      textRenderer.method_22942(line, x, y, 553648127, false, matrix, vertexConsumers, layer, backgroundColor, light);
      if (!sneaking) {
         textRenderer.method_22942(line, x, y, -1, false, matrix, vertexConsumers, class_6415.field_33993, 0, light);
      }
   }

   private LineBreakFormattor.SplitResult splitOrderedText(class_5481 orderedText) {
      List<class_5481> lines = new ArrayList<>();
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

   private class_5481 createOrderedText(List<LineBreakFormattor.MappedCharacter> chars) {
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

   private record MappedCharacter(class_2583 style, int codePoint) {
   }

   private record SplitResult(List<class_5481> lines, boolean hasLineBreaks) {
   }
}
