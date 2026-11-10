package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.module.impl.render.TargetHUD;
import dev.southside.nanovg.NanoUtil;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.misc.BezierUtil;
import dev.southside.utils.render.animation.TimerUtil;
import java.awt.Color;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.gui.PlayerSkinDrawer;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class TargetHud7 extends Hud {
   private final TargetHUD parent;

   public TargetHud7(float x, float y, int positionAlignment, float scale, TargetHUD parent) {
      super(x, y, positionAlignment, scale);
      this.parent = parent;
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 120.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 42.0F * scale;
   }

   public static class TargetHUDComponent {
      public static final float BASE_WIDTH = 120.0F;
      public static final float COMPONENT_SPACING = 50.0F;
      private static final float MAIN_PANEL_HEIGHT = 28.0F;
      private static final float INFO_PANEL_HEIGHT = 12.0F;
      private static final float PANEL_GAP = 2.0F;
      public static final float VISIBLE_HEIGHT = 42.0F;
      private static final float PADDING = 6.0F;
      private static final float HEAD_SIZE = 24.0F;
      private static final float HEAD_OFFSET_X = 5.0F;
      private static final float HEAD_OFFSET_Y = 2.0F;
      private static final int BLOCK_HISTORY = 20;
      private static final long BLOCK_MASK = 1048575L;
      private static final float TEXT_BASELINE_FIX = -5.5F;
      public final TimerUtil updateTimer = new TimerUtil();
      public final BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private final BezierUtil health = new BezierUtil(3.0F, 1.0F);
      private long blockHistoryMask = 0L;
      public AbstractClientPlayerEntity entity;

      public TargetHUDComponent(AbstractClientPlayerEntity entity) {
         this.entity = entity;
      }

      public void draw(long vg, float scale, float originX, float yOffset) {
         float animY = this.y.get();
         if (!(animY < -(50.0F * scale))) {
            NanoVGHelper helper = NanoVGHelper.INSTANCE;
            float baseY = yOffset + animY;
            float width = 120.0F * scale;
            float mainH = 28.0F * scale;
            float gap = 2.0F * scale;
            float infoH = 12.0F * scale;
            float infoY = baseY + mainH + gap;
            this.sampleBlockHistory();
            int bg = new Color(0, 0, 0, 150).getRGB();
            float radius = 3.0F * scale;
            drawBloom(vg, originX, baseY, width, mainH, radius, 10.0F * scale, new Color(15, 15, 30, 50));
            drawBloom(vg, originX, infoY, width, infoH, radius, 8.0F * scale, new Color(15, 15, 30, 40));
            helper.drawRoundedRect(vg, originX, baseY, width, mainH, bg, radius);
            helper.drawRoundedRect(vg, originX, infoY, width, infoH, bg, radius);
            float pad = 6.0F * scale;
            float headX = originX + 5.0F * scale;
            float contentX = headX + 24.0F * scale + pad;
            String name = this.entity.getDisplayName().getString();
            float nameSize = 9.0F * scale;
            float nameMaxW = originX + width - contentX - pad;
            name = fitText(helper, vg, name, nameMaxW, nameSize, Fonts.SEMIBOLD);
            float nameY = baseY + 4.0F * scale - -5.5F * scale;
            helper.drawText(vg, name, contentX, nameY, Color.WHITE.getRGB(), nameSize, Fonts.SEMIBOLD);
            float maxHealth = Math.max(this.entity.getMaxHealth(), 1.0F);
            float rate = MathUtils.clamp(this.entity.getHealth() / maxHealth, 0.0F, 1.0F);
            this.health.update(rate);
            float easedRate = this.health.get();
            float barHeight = 2.5F * scale;
            float barY = baseY + 19.0F * scale;
            float barW = originX + width - contentX - pad;
            barW = Math.max(1.0F, barW);
            helper.drawRoundedRect(vg, contentX, barY, barW, barHeight, new Color(40, 40, 40, 200).getRGB(), barHeight / 2.0F);
            if (easedRate > 0.02F) {
               float fillW = barW * easedRate;
               drawBloom(vg, contentX, barY, fillW, barHeight, barHeight / 2.0F, 5.0F * scale, new Color(120, 160, 255, 55));
               helper.drawRoundedRect(vg, contentX, barY, fillW, barHeight, Color.WHITE.getRGB(), barHeight / 2.0F);
            }

            String extra = String.format("Health: %.1f  Block Rate: %d%%", this.entity.getHealth(), this.getBlockPercentage());
            float infoSize = 8.0F * scale;
            float infoTextX = originX + pad;
            float infoTextY = infoY + (infoH - infoSize) / 2.0F - -5.0F * scale;
            helper.drawText(vg, extra, infoTextX, infoTextY, new Color(220, 220, 220).getRGB(), infoSize, Fonts.REGULAR);
         }
      }

      public void drawHead(DrawContext context, float scale, float originX, float yOffset) {
         float animY = this.y.get();
         if (!(animY < -(50.0F * scale))) {
            float baseY = yOffset + animY;
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(originX + 5.0F * scale, baseY + 2.0F * scale, 0.0F);
            int size = Math.max(1, Math.round(24.0F * scale));
            PlayerSkinDrawer.draw(context, this.entity.getSkinTextures(), 0, 0, size);
            matrices.pop();
         }
      }

      private static String fitText(NanoVGHelper helper, long vg, String text, float maxWidth, float size, Font font) {
         if (text == null) {
            return "";
         } else if (helper.getTextWidth(vg, text, size, font) <= maxWidth) {
            return text;
         } else {
            String ellipsis = "...";
            float ellW = helper.getTextWidth(vg, "...", size, font);
            if (ellW > maxWidth) {
               return "";
            } else {
               int lo = 0;
               int hi = text.length();

               while (lo < hi) {
                  int mid = lo + hi + 1 >>> 1;
                  String sub = text.substring(0, mid) + "...";
                  if (helper.getTextWidth(vg, sub, size, font) <= maxWidth) {
                     lo = mid;
                  } else {
                     hi = mid - 1;
                  }
               }

               return text.substring(0, lo) + "...";
            }
         }
      }

      private void sampleBlockHistory() {
         boolean blocking = this.entity.isUsingItem() && this.isBlockingItem(this.entity.getActiveItem());
         this.blockHistoryMask = (this.blockHistoryMask << 1 | (blocking ? 1L : 0L)) & 1048575L;
      }

      private boolean isBlockingItem(ItemStack stack) {
         return stack != null && !stack.isEmpty() ? stack.getItem() instanceof SwordItem || stack.isOf(Items.SHIELD) : false;
      }

      private int getBlockPercentage() {
         int ticks = Long.bitCount(this.blockHistoryMask);
         return Math.min(100, Math.round(ticks / 20.0F * 100.0F));
      }

      private static NVGColor toNVGColor(Color color) {
         return NanoUtil.getColor(color);
      }

      private static void drawBloom(long vg, float x, float y, float w, float h, float radius, float shadowWidth, Color color) {
         NVGPaint paint = NVGPaint.calloc();
         NVGColor inner = toNVGColor(color);
         NVGColor outer = toNVGColor(new Color(0, 0, 0, 0));
         NanoVG.nvgBoxGradient(vg, x, y, w, h, radius, shadowWidth, inner, outer, paint);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRect(vg, x - shadowWidth, y - shadowWidth, w + shadowWidth * 2.0F, h + shadowWidth * 2.0F);
         NanoVG.nvgRoundedRect(vg, x, y, w, h, radius);
         NanoVG.nvgPathWinding(vg, 2);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
         paint.free();
         inner.free();
         outer.free();
      }
   }
}
