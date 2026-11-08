package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.module.impl.client.Theme;
import dev.southside.module.impl.render.TargetHUD;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.misc.BezierUtil;
import dev.southside.utils.render.animation.TimerUtil;
import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_742;

public class TargetHud3 extends Hud {
   private final TargetHUD parent;

   public TargetHud3(float x, float y, int positionAlignment, float scale, TargetHUD parent) {
      super(x, y, positionAlignment, scale);
      this.parent = parent;
   }

   @Override
   protected void draw(class_332 context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 200.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 40.0F * scale;
   }

   public static class TargetHUDComponent {
      public final TimerUtil updateTimer = new TimerUtil();
      public final BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private static final float BASE_WIDTH = 160.0F;
      private static final float BASE_HEIGHT = 40.0F;
      public class_742 entity;

      public TargetHUDComponent(class_742 entity) {
         this.entity = entity;
      }

      public void draw(long vg, float scale, float x, float yOffset) {
         if (!(this.y.get() < -(40.0F * scale))) {
            float currentY = this.y.get() + yOffset;
            String text = this.entity.method_5476().getString() + " " + MathUtils.round(this.entity.method_6032(), 1) + " HP";
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            float mainSize = 28.0F * scale;
            float interval = 8.0F * scale;
            float textWidth = nanoVGHelper.getTextWidth(vg, text, mainSize, Fonts.SEMIBOLD);
            float textHeight = nanoVGHelper.getTextHeight(vg, mainSize, Fonts.SEMIBOLD);
            float rectWidth = interval * 2.0F + textWidth;
            float rectHeight = textHeight + interval * 2.0F;
            nanoVGHelper.drawRoundedRect(vg, x, currentY, rectWidth, rectHeight, new Color(0, 0, 0, 100).getRGB(), 8.0F * scale);
            nanoVGHelper.drawDropShadow(vg, x, currentY, rectWidth, rectHeight, 10.0F, 0.01F, 8.0F * scale, new Color(0, 0, 0, 80));
            nanoVGHelper.drawText(vg, text, x + interval, currentY + interval + textHeight / 2.0F, Theme.getColor(0).getRGB(), mainSize, Fonts.SEMIBOLD);
         }
      }
   }
}
