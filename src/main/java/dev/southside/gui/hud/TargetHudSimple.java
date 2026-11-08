package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.utils.misc.BezierUtil;
import dev.southside.utils.render.animation.TimerUtil;
import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_7532;

public class TargetHudSimple extends Hud {
   private static final float BASE_WIDTH = 170.0F;
   private static final float BASE_HEIGHT = 44.0F;
   private static final float PAD = 6.0F;
   private static final float HEAD_SIZE = 32.0F;
   private static final float TEXT_LEFT = 44.0F;
   private static final Color BG_COLOR = new Color(0, 0, 0, 180);
   private static final Color HEALTH_TRACK = new Color(255, 255, 255, 30);
   private static final Color HEALTH_FILL = new Color(255, 255, 255, 220);
   private static final Color TEXT_SECONDARY = new Color(180, 180, 180);

   @Override
   protected void draw(class_332 context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 170.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 44.0F * scale;
   }

   public static class TargetHUDComponent {
      public final TimerUtil updateTimer = new TimerUtil();
      public final BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private final BezierUtil health = new BezierUtil(3.0F, 1.0F);
      public class_742 entity;

      public TargetHUDComponent(class_742 entity) {
         this.entity = entity;
      }

      public void draw(long vg, float scale, float x, float yOffset) {
         if (!(this.y.get() < -(44.0F * scale))) {
            float currentY = this.y.get() + yOffset;
            float width = 170.0F * scale;
            float height = 44.0F * scale;
            float headSize = 32.0F * scale;
            float headTop = currentY + (height - headSize) / 2.0F;
            float headBottom = headTop + headSize;
            float textX = x + 44.0F * scale;
            float barWidth = width - 6.0F * scale - 44.0F * scale;
            NanoVGHelper helper = NanoVGHelper.INSTANCE;
            helper.drawRect(vg, x, currentY, width, height, TargetHudSimple.BG_COLOR.getRGB());
            float barHeight = 3.0F * scale;
            float barY = headBottom - barHeight - 1.0F * scale;
            float nameSize = 10.0F * scale;
            float nameH = helper.getTextHeight(vg, nameSize, Fonts.SEMIBOLD);
            float nameY = headTop + nameH / 2.0F;
            helper.drawTextWithFormatting(vg, this.entity.method_5476().getString(), textX, nameY, Color.WHITE.getRGB(), nameSize, Fonts.SEMIBOLD);
            float healthPercentage = this.entity.method_6032() / this.entity.method_6063();
            this.health.update(healthPercentage);
            String hpText = String.format("%.1f HP", this.entity.method_6032());
            float hpSize = 8.0F * scale;
            float hpY = headTop + headSize * 0.5F;
            helper.drawText(vg, hpText, textX, hpY, TargetHudSimple.TEXT_SECONDARY.getRGB(), hpSize, Fonts.REGULAR);
            helper.drawRect(vg, textX, barY, barWidth, barHeight, TargetHudSimple.HEALTH_TRACK.getRGB());
            float filledWidth = barWidth * this.health.get();
            if (filledWidth > 0.5F) {
               helper.drawRect(vg, textX, barY, filledWidth, barHeight, TargetHudSimple.HEALTH_FILL.getRGB());
            }
         }
      }

      public void drawBigHead(class_332 context, float scale, float x, float y) {
         class_4587 matrices = context.method_51448();
         matrices.method_22903();
         matrices.method_46416(x, y + this.y.get(), 0.0F);
         float headSize = 32.0F * scale;
         float height = 44.0F * scale;
         float headX = (44.0F * scale - headSize) / 2.0F;
         float headY = (height - headSize) / 2.0F;
         class_7532.method_52722(context, this.entity.method_52814(), (int)headX, (int)headY, (int)headSize);
         matrices.method_22909();
      }
   }
}
