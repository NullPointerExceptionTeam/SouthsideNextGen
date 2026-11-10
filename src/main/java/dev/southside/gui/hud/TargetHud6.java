package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.module.impl.render.TargetHUD;
import dev.southside.utils.misc.BezierUtil;
import dev.southside.utils.render.animation.TimerUtil;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class TargetHud6 extends Hud {
   private final TargetHUD parent;

   public TargetHud6(float x, float y, int positionAlignment, float scale, TargetHUD parent) {
      super(x, y, positionAlignment, scale);
      this.parent = parent;
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 280.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 88.0F * scale;
   }

   public static class TargetHUDComponent {
      public static final float BASE_WIDTH = 280.0F;
      public static final float BASE_HEIGHT = 88.0F;
      public final TimerUtil updateTimer = new TimerUtil();
      public final BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private final BezierUtil health = new BezierUtil(4.0F, 1.0F);
      public AbstractClientPlayerEntity entity;

      public TargetHUDComponent(AbstractClientPlayerEntity entity) {
         this.entity = entity;
      }

      public void draw(long vg, float scale, float originX, float yOffset) {
         if (!(this.y.get() < -(88.0F * scale))) {
            float containerY = yOffset + this.y.get();
            float width = 280.0F * scale;
            float height = 88.0F * scale;
            float radius = 16.0F * scale;
            float padding = 18.0F * scale;
            NanoVGHelper helper = NanoVGHelper.INSTANCE;
            int baseAlpha = Math.round(51.0F);
            helper.drawRoundedRect(vg, originX, containerY, width, height, new Color(0, 0, 0, baseAlpha).getRGB(), radius);
            helper.drawRoundedRect(
               vg,
               originX + 1.0F * scale,
               containerY + 1.0F * scale,
               width - 2.0F * scale,
               height - 2.0F * scale,
               new Color(8, 9, 14, (int)(baseAlpha * 0.9F)).getRGB(),
               radius - 1.0F * scale
            );
            float maxHealth = Math.max(this.entity.getMaxHealth(), 1.0F);
            float rawPercent = Math.max(0.0F, Math.min(this.entity.getHealth() / maxHealth, 1.0F));
            this.health.update(rawPercent);
            float easedPercent = this.health.get();
            int percentValue = Math.round(easedPercent * 100.0F);
            String name = this.entity.getDisplayName().getString();
            float nameSize = 18.0F * scale;
            float percentSize = 15.0F * scale;
            float textHeight = helper.getTextHeight(vg, nameSize, Fonts.SEMIBOLD);
            float textY = containerY + padding + textHeight / 2.0F;
            helper.drawText(vg, name, originX + padding, textY, Color.WHITE.getRGB(), nameSize, Fonts.SEMIBOLD);
            String percentText = percentValue + "%";
            float percentWidth = helper.getTextWidth(vg, percentText, percentSize, Fonts.SEMIBOLD);
            helper.drawText(vg, percentText, originX + width - padding - percentWidth, textY, Color.WHITE.getRGB(), percentSize, Fonts.SEMIBOLD);
            float barWidth = width - padding * 2.0F;
            float barHeight = 8.0F * scale;
            float barX = originX + padding;
            float barY = containerY + height - padding - barHeight;
            helper.drawRoundedRect(vg, barX, barY, barWidth, barHeight, new Color(18, 20, 28, 180).getRGB(), barHeight / 2.0F);
            helper.drawRoundedRect(vg, barX, barY, barWidth * easedPercent, barHeight, new Color(112, 148, 255).getRGB(), barHeight / 2.0F);
            helper.drawRoundedRect(vg, barX, barY, barWidth, barHeight / 2.0F, new Color(255, 255, 255, 25).getRGB(), barHeight / 2.0F);
         }
      }
   }
}
