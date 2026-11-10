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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.gui.PlayerSkinDrawer;

public class TargetHud5 extends Hud {
   private final TargetHUD parent;

   public TargetHud5(float x, float y, int positionAlignment, float scale, TargetHUD parent) {
      super(x, y, positionAlignment, scale);
      this.parent = parent;
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 160.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 40.0F * scale;
   }

   public static class TargetHUDComponent {
      public final TimerUtil updateTimer = new TimerUtil();
      public final BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private final BezierUtil health = new BezierUtil(3.0F, 1.0F);
      private static final float BASE_WIDTH = 160.0F;
      private static final float BASE_HEIGHT = 40.0F;
      public AbstractClientPlayerEntity entity;

      public TargetHUDComponent(AbstractClientPlayerEntity entity) {
         this.entity = entity;
      }

      public void draw(long vg, float scale, float x, float yOffset) {
         if (!(this.y.get() < -(40.0F * scale))) {
            float currentY = this.y.get() + yOffset;
            float width = 160.0F * scale;
            float height = 40.0F * scale;
            float borderSize = 1.0F * scale;
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.drawRect(vg, x, currentY, width, height, new Color(255, 255, 255, 50).getRGB());
            nanoVGHelper.drawRect(
               vg, x + borderSize, currentY + borderSize, width - borderSize * 2.0F, height - borderSize * 2.0F, new Color(20, 20, 20, 100).getRGB()
            );
            nanoVGHelper.drawTextWithFormatting(
               vg, this.entity.getDisplayName().getString(), x + 44.0F * scale, currentY + 11.0F * scale, -1, 10.0F * scale, Fonts.REGULAR
            );
            float healthBarX = x + 44.0F * scale;
            float healthBarY = currentY + 25.0F * scale;
            float healthBarWidth = width - 54.0F * scale;
            float healthBarHeight = 6.0F * scale;
            nanoVGHelper.drawRect(vg, healthBarX, healthBarY, healthBarWidth, healthBarHeight, new Color(0, 0, 0, 80).getRGB());
            float healthPercentage = this.entity.getHealth() / this.entity.getMaxHealth();
            this.health.update(healthPercentage);
            float currentHealthWidth = healthBarWidth * this.health.get();
            int healthColor = Color.HSBtoRGB(Math.max(0.0F, healthPercentage) / 3.0F, 1.0F, 1.0F);
            nanoVGHelper.drawRect(vg, healthBarX, healthBarY, currentHealthWidth, healthBarHeight, healthColor);
         }
      }

      public void drawBigHead(DrawContext context, float scale, float x, float y) {
         MatrixStack matrices = context.getMatrices();
         matrices.push();
         matrices.translate(x, y + this.y.get(), 0.0F);
         float headSize = 28.0F * scale;
         float headX = 8.0F * scale;
         float headY = (40.0F * scale - headSize) / 2.0F;
         PlayerSkinDrawer.draw(context, this.entity.getSkinTextures(), (int)headX, (int)headY, (int)headSize);
         matrices.pop();
      }
   }
}
