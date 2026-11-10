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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.gui.PlayerSkinDrawer;

public class TargetHud4 extends Hud {
   private final TargetHUD parent;

   public TargetHud4(float x, float y, int positionAlignment, float scale, TargetHUD parent) {
      super(x, y, positionAlignment, scale);
      this.parent = parent;
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
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
      private final BezierUtil health = new BezierUtil(3.0F, 1.0F);
      private static final float BASE_WIDTH = 200.0F;
      private static final float BASE_HEIGHT = 40.0F;
      public AbstractClientPlayerEntity entity;

      public TargetHUDComponent(AbstractClientPlayerEntity entity) {
         this.entity = entity;
      }

      public void draw(long vg, float scale, float x, float yOffset) {
         if (!(this.y.get() < -(40.0F * scale))) {
            float currentY = this.y.get() + yOffset;
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            float mainSize = 20.0F * scale;
            float interval = 6.0F * scale;
            String text = this.entity.getDisplayName().getString() + " " + MathUtils.round(this.entity.getHealth(), 1) + " HP";
            float textWidth = nanoVGHelper.getTextWidth(vg, text, mainSize, Fonts.SEMIBOLD);
            float textHeight = nanoVGHelper.getTextHeight(vg, mainSize, Fonts.SEMIBOLD);
            float rectWidth = interval * 2.0F + textWidth + 40.0F * scale;
            float rectHeight = 40.0F * scale;
            nanoVGHelper.drawRoundedRect(vg, x, currentY, rectWidth, rectHeight, new Color(0, 0, 0, 100).getRGB(), 8.0F * scale);
            nanoVGHelper.drawDropShadow(vg, x, currentY, rectWidth, rectHeight, 10.0F, 0.01F, 8.0F * scale, new Color(0, 0, 0, 80));
            nanoVGHelper.drawText(
               vg, text, x + interval + 40.0F * scale, currentY + interval + textHeight / 2.0F, Theme.getColor(0).getRGB(), mainSize, Fonts.SEMIBOLD
            );
            float healthBarX = x + interval + 40.0F * scale;
            float healthBarY = currentY + rectHeight - interval - 6.0F * scale;
            float healthBarHeight = 6.0F * scale;
            nanoVGHelper.drawRoundedRect(vg, healthBarX, healthBarY, textWidth, healthBarHeight, new Color(0, 0, 0, 80).getRGB(), 3.0F * scale);
            float healthPercentage = this.entity.getHealth() / this.entity.getMaxHealth();
            this.health.update(healthPercentage);
            float currentHealthWidth = textWidth * this.health.get();
            int healthColor = Color.HSBtoRGB(Math.max(0.0F, healthPercentage) / 3.0F, 1.0F, 1.0F);
            nanoVGHelper.drawRoundedRect(vg, healthBarX, healthBarY, currentHealthWidth, healthBarHeight, healthColor, 3.0F * scale);
         }
      }

      public void drawAvatar(DrawContext context, float scale, float x, float y) {
         MatrixStack matrices = context.getMatrices();
         matrices.push();
         matrices.translate(x, y + this.y.get(), 0.0F);
         float headSize = 32.0F * scale;
         float headX = 8.0F * scale;
         float headY = (40.0F * scale - headSize) / 2.0F;
         PlayerSkinDrawer.draw(context, this.entity.getSkinTextures(), (int)headX, (int)headY, (int)headSize);
         matrices.pop();
      }
   }
}
