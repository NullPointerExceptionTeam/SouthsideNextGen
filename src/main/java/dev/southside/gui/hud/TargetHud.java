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
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.misc.BezierUtil;
import dev.southside.utils.render.animation.TimerUtil;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;

public class TargetHud extends Hud {
   private final TargetHUD parent;

   public TargetHud(float x, float y, int positionAlignment, float scale, TargetHUD parent) {
      super(x, y, positionAlignment, scale);
      this.parent = parent;
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
      nanoVGHelper.setupAndDraw(true, vg -> {
         Scissor scissor = scissorHelper.scissor(vg, x - 5.0F, y, UResolution.getScaledWidth(), UResolution.getScaledHeight());
         float targetY = 0.0F;
         ArrayList<TargetHud.TargetHUDComponent> components = new ArrayList<>(this.parent.targets.values());

         for (int i = 0; i < components.size(); i++) {
            TargetHud.TargetHUDComponent componentx = components.get(i);
            if (componentx.updateTimer.hasReached(10000.0)) {
               components.remove(i);
               i--;
            } else {
               if (componentx.updateTimer.hasReached(1000.0)) {
                  componentx.y.update(-50.0F * scale);
               } else {
                  componentx.y.update(targetY);
                  targetY += 57.5F * scale;
               }

               componentx.draw(vg, scale, x, y);
            }
         }

         scissorHelper.resetScissor(vg, scissor);
      });
      context.getMatrices().push();
      context.enableScissor((int)(x - 5.0F * scale), (int)y, UResolution.getScaledWidth(), UResolution.getScaledHeight());

      for (TargetHud.TargetHUDComponent component : new ArrayList<>(this.parent.targets.values())) {
         component.drawBigHead(context, scale, x, y);
      }

      context.disableScissor();
      context.getMatrices().pop();
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 160.0F;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 40.0F;
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
            String extraMessage = "Health: " + MathUtils.round(this.entity.getHealth(), 1);
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            float rad = 7.5F * scale;
            nanoVGHelper.drawRoundedRect(vg, x, y, 197.5F * scale, 52.5F * scale, new Color(34, 35, 38, alpha).getRGB(), rad);
            Scissor scissor = scissorHelper.scissor(vg, x, y, 4.0F * scale, 52.5F * scale);
            if (this.entity == KillAura.target) {
               this.color.update(1.0F);
               this.color.set(1.0F);
               nanoVGHelper.drawRoundedRect(vg, x, y, 197.5F * scale, 52.5F * scale, Color.WHITE.getRGB(), rad);
               scissorHelper.resetScissor(vg, scissor);
               nanoVGHelper.drawDropShadow(vg, x + 3.0F * scale, y, 1.0F * scale, 52.5F * scale, 8.5F, 0.01F, rad, new Color(255, 255, 255, 150));
            } else {
               this.color.update(0.0F);
               nanoVGHelper.drawRoundedRect(
                  vg,
                  x,
                  y,
                  197.5F * scale,
                  52.5F * scale,
                  new Color(255, 255, 255, (int)ColorUtil.interpolateFloat(0.0F, 150.0F, this.color.get())).getRGB(),
                  rad
               );
               scissorHelper.resetScissor(vg, scissor);
               nanoVGHelper.drawDropShadow(
                  vg,
                  x + 3.0F * scale,
                  y,
                  1.0F * scale,
                  52.5F * scale,
                  8.5F,
                  0.01F,
                  rad,
                  new Color(255, 255, 255, (int)ColorUtil.interpolateFloat(0.0F, 150.0F, this.color.get()))
               );
            }

            int fontColor = new Color(255, 255, 255, 255).getRGB();
            float size = 30.0F * scale;
            float h1 = nanoVGHelper.getTextHeight(vg, 13.0F * scale, Fonts.SEMIBOLD);
            float yy = (52.5F * scale - size) / 2.0F + h1 / 2.0F;
            nanoVGHelper.drawTextWithFormatting(
               vg, this.entity.getDisplayName().getString(), (int)(x + 58.0F * scale), y + yy, fontColor, 13.0F * scale, Fonts.SEMIBOLD
            );
            nanoVGHelper.drawTextWithFormatting(vg, extraMessage, x + 58.0F * scale, y + yy + h1 + 4.5F * scale, fontColor, 8.0F * scale, Fonts.REGULAR);
            float rate = this.entity.getHealth() / this.entity.getMaxHealth();
            this.health.update(rate);
            float progressBarWidth = 197.5F * scale * this.health.get();
            Scissor scissor1 = scissorHelper.scissor(vg, x, y, progressBarWidth, 52.5F * scale);
            nanoVGHelper.drawRoundedRect(vg, x, y, 197.5F * scale, 52.5F * scale, new Color(255, 255, 255, 30).getRGB(), rad);
            scissorHelper.resetScissor(vg, scissor1);
         }
      }

      public void drawBigHead(DrawContext context, float scale, float x, float y) {
         float size = 30.0F * scale;
         float headX = x + 16.0F * scale;
         float headY = y + this.y.get() + (52.5F * scale - size) / 2.0F;
         float radius = 6.0F * scale;
         ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
         NanoVGHelper.INSTANCE.setupAndDraw(true, vg -> {
            Scissor scissor = scissorHelper.scissor(vg, x - 5.0F * scale, y, UResolution.getScaledWidth(), UResolution.getScaledHeight());
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
