package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.color.ColorUtil;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.render.TargetHUD;
import dev.southside.utils.misc.BezierUtil;
import dev.southside.utils.render.animation.TimerUtil;
import java.awt.Color;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;

public class TargetHud8 extends Hud {
   private final TargetHUD parent;

   public TargetHud8(float x, float y, int positionAlignment, float scale, TargetHUD parent) {
      super(x, y, positionAlignment, scale);
      this.parent = parent;
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 160.0F;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 52.5F;
   }

   public static class TargetHUDComponent {
      public final TimerUtil updateTimer = new TimerUtil();
      public BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private BezierUtil health = new BezierUtil(3.0F, 1.0F);
      private static final float height = 30.0F;
      public AbstractClientPlayerEntity entity;
      public BezierUtil color = new BezierUtil(10.0F, 0.0F);
      private int headImage = -1;
      private int headTextureId = -1;
      private int headImageSize = -1;

      public TargetHUDComponent(AbstractClientPlayerEntity entity) {
         this.entity = entity;
      }

      public void draw(long vg, float scale, float x, float yOffset) {
         if (!(this.y.get() < -30.0F * scale)) {
            float y = this.y.get() + yOffset;
            int alpha = 191;
            String extraMessage = String.format("Health: %.1f /  %.1f", this.entity.getHealth(), this.entity.getMaxHealth());
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            float width = Math.max(
               Math.max(
                  nanoVGHelper.getTextWidth(vg, extraMessage, 14.0F * scale, Fonts.REGULAR) + 80.0F * scale,
                  nanoVGHelper.getTextWidth(vg, this.entity.getDisplayName().getString(), 18.0F * scale, Fonts.SEMIBOLD) + 80.0F * scale
               ),
               197.5F * scale
            );
            float rad = 9.0F * scale;
            float progressBarWidth = width * this.health.get();
            int fontColor = Color.WHITE.getRGB();
            int fontColorReverted = Color.BLACK.getRGB();
            float size = 30.0F * scale;
            float h1 = nanoVGHelper.getTextHeight(vg, 13.0F * scale, Fonts.SEMIBOLD);
            float yy = (62.5F * scale - size) / 2.0F + h1 / 2.0F;
            if (this.entity == KillAura.target) {
               this.color.update(1.0F);
               this.color.set(1.0F);
               Scissor scissor = scissorHelper.scissor(vg, x + progressBarWidth, y, width - progressBarWidth, 62.5F * scale);
               nanoVGHelper.drawRoundedRect(vg, x, y, width, 62.5F * scale, new Color(34, 35, 38, alpha).getRGB(), rad);
               nanoVGHelper.drawTextWithFormatting(
                  vg, this.entity.getDisplayName().getString(), (int)(x + 60.0F * scale), y + yy, fontColor, 18.0F * scale, Fonts.SEMIBOLD
               );
               nanoVGHelper.drawTextWithFormatting(vg, extraMessage, x + 60.0F * scale, y + yy + h1 + 7.5F * scale, fontColor, 14.0F * scale, Fonts.REGULAR);
               scissorHelper.resetScissor(vg, scissor);
               Scissor scissor1 = scissorHelper.scissor(vg, x, y, progressBarWidth, 62.5F * scale);
               nanoVGHelper.drawRoundedRect(vg, x, y, width, 62.5F * scale, new Color(255, 255, 255, 200).getRGB(), rad);
               nanoVGHelper.drawTextWithFormatting(
                  vg, this.entity.getDisplayName().getString(), (int)(x + 60.0F * scale), y + yy, fontColorReverted, 18.0F * scale, Fonts.SEMIBOLD
               );
               nanoVGHelper.drawTextWithFormatting(
                  vg, extraMessage, x + 60.0F * scale, y + yy + h1 + 7.5F * scale, fontColorReverted, 14.0F * scale, Fonts.REGULAR
               );
               scissorHelper.resetScissor(vg, scissor1);
            } else {
               this.color.update(0.0F);
               Scissor scissor = scissorHelper.scissor(vg, x + progressBarWidth, y, width - progressBarWidth, 62.5F * scale);
               nanoVGHelper.drawRoundedRect(vg, x, y, width, 62.5F * scale, new Color(34, 35, 38, alpha).getRGB(), rad);
               nanoVGHelper.drawTextWithFormatting(
                  vg, this.entity.getDisplayName().getString(), (int)(x + 60.0F * scale), y + yy, fontColor, 18.0F * scale, Fonts.SEMIBOLD
               );
               nanoVGHelper.drawTextWithFormatting(vg, extraMessage, x + 60.0F * scale, y + yy + h1 + 7.5F * scale, fontColor, 14.0F * scale, Fonts.REGULAR);
               scissorHelper.resetScissor(vg, scissor);
               Scissor scissor1 = scissorHelper.scissor(vg, x, y, progressBarWidth, 62.5F * scale);
               nanoVGHelper.drawRoundedRect(
                  vg, x, y, width, 62.5F * scale, new Color(255, 255, 255, (int)ColorUtil.interpolateFloat(140.0F, 200.0F, this.color.get())).getRGB(), rad
               );
               nanoVGHelper.drawTextWithFormatting(
                  vg, this.entity.getDisplayName().getString(), (int)(x + 60.0F * scale), y + yy, fontColorReverted, 18.0F * scale, Fonts.SEMIBOLD
               );
               nanoVGHelper.drawTextWithFormatting(
                  vg, extraMessage, x + 60.0F * scale, y + yy + h1 + 7.5F * scale, fontColorReverted, 14.0F * scale, Fonts.REGULAR
               );
               scissorHelper.resetScissor(vg, scissor1);
            }

            float rate = this.entity.getHealth() / this.entity.getMaxHealth();
            this.health.update(rate);
         }
      }

      public void drawBigHead(DrawContext context, float scale, float x, float y) {
         float size = 40.0F * scale;
         float headX = x + 12.0F * scale;
         float headY = y + this.y.get() + (61.25F * scale - size) / 2.0F;
         float radius = 3.0F * scale;
         ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
         NanoVGHelper.INSTANCE.setupAndDraw(true, vg -> {
            Scissor scissor = scissorHelper.scissor(vg, x - 5.0F * scale, y + 1.0F, UResolution.getScaledWidth(), UResolution.getScaledHeight());
            this.drawRoundedHead(vg, headX, headY, size, radius);
            scissorHelper.resetScissor(vg, scissor);
         });
      }

      private void drawRoundedHead(long vg, float x, float y, float size, float radius) {
         Identifier skin = this.entity.getSkinTextures().texture();
         if (skin != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            AbstractTexture texture = mc.getTextureManager().getTexture(skin);
            if (texture != null) {
               int textureId = texture.getGlId();
               int imageSize = Math.max(1, Math.round(size));
               if (this.headImage == -1 || this.headTextureId != textureId || this.headImageSize != imageSize) {
                  this.headImage = NanoVGGL2.nvglCreateImageFromHandle(vg, textureId, imageSize, -imageSize, 0);
                  this.headTextureId = textureId;
                  this.headImageSize = imageSize;
               }

               if (this.headImage != -1) {
                  NVGPaint paint = NVGPaint.calloc();
                  float uvScale = 8.0F;
                  float sampleY = y - size / 4.0F * uvScale / 2.0F + 0.1F;
                  float sampleW = size * uvScale;
                  float sampleH = size * uvScale;
                  NanoVG.nvgImagePattern(vg, x - size / 4.0F * uvScale / 2.0F, sampleY, sampleW, sampleH, 0.0F, this.headImage, 1.0F, paint);
                  NanoVG.nvgBeginPath(vg);
                  NanoVG.nvgRoundedRect(vg, x, y, size, size, radius);
                  NanoVG.nvgFillPaint(vg, paint);
                  NanoVG.nvgFill(vg);
                  NanoVG.nvgClosePath(vg);
                  NanoVG.nvgImagePattern(vg, x - size * 3.25F * uvScale / 2.0F, sampleY, sampleW, sampleH, 0.0F, this.headImage, 1.0F, paint);
                  NanoVG.nvgBeginPath(vg);
                  NanoVG.nvgRoundedRect(vg, x, y - 0.1F, size, size, radius);
                  NanoVG.nvgFillPaint(vg, paint);
                  NanoVG.nvgFill(vg);
                  NanoVG.nvgClosePath(vg);
                  paint.free();
               }
            }
         }
      }
   }
}
