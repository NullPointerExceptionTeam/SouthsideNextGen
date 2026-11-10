package dev.southside.gui.hud.jello;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.asset.NVGAsset;
import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.render.jello.animation.Animation;
import dev.southside.utils.render.jello.animation.Easing;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Generated;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class ActiveMods implements MinecraftAccessor {
   public static Font font = Fonts.REGULAR;
   private final List<ActiveMods.ModuleNode> modules = new ArrayList<>();

   private static void drawImage(long ng, NVGAsset image, float x, float y, float u, float v, float w, float h, float uw, float uh, float alpha) {
      float texW = image.getWidth();
      float texH = image.getHeight();
      float u0 = u / texW;
      float v0 = v / texH;
      float u1 = (u + uw) / texW;
      float v1 = (v + uh) / texH;
      float us = u1 - u0;
      float vs = v1 - v0;
      NVGPaint imagePaint = NVGPaint.calloc();
      NanoVG.nvgImagePattern(ng, x, y, w, h, 0.0F, image.getImage(), alpha, imagePaint);
      FloatBuffer origXform = imagePaint.xform();
      float a = origXform.get(0);
      float b = origXform.get(1);
      float c = origXform.get(2);
      float d = origXform.get(3);
      float e = origXform.get(4);
      float f = origXform.get(5);
      origXform.put(0, us * a);
      origXform.put(1, vs * b);
      origXform.put(2, us * c);
      origXform.put(3, vs * d);
      origXform.put(4, us * e + u0);
      origXform.put(5, vs * f + v0);
      NanoVG.nvgBeginPath(ng);
      NanoVG.nvgRect(ng, x, y, w, h);
      NanoVG.nvgFillPaint(ng, imagePaint);
      NanoVG.nvgFill(ng);
      imagePaint.free();
   }

   public static void drawTexture(long nvg, String image, float x, float y, float w, float h, float alpha) {
      NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      AtomicBoolean loaded = new AtomicBoolean(false);
      String imagePath = "/assets/southside/textures/gui/hud/arraylist/" + image;
      nanovg.setupAndDraw(vg1 -> loaded.set(assetHelper.loadImage(vg1, imagePath, ActiveMods.class)));
      if (loaded.get()) {
         NVGAsset asset = assetHelper.getNVGImage(imagePath);
         float texW = asset.getWidth();
         float texH = asset.getHeight();
         drawImage(nvg, asset, x, y, 0.0F, 0.0F, w, h, texW, texH, alpha);
      }
   }

   public ActiveMods() {
      for (Module module : Southside.INSTANCE.getModuleManager().getModules()) {
         if (module.getCategory() != Category.Render) {
            this.modules.add(new ActiveMods.ModuleNode(module, this));
         }
      }
   }

   public void draw(Font font) {
      this.modules.sort(new ModuleSorter(font));
      ActiveMods.font = font;
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      float[] height = new float[1];
      nanoVGHelper.setupAndDraw(true, vg -> height[0] = nanoVGHelper.getTextHeight(vg, 20.0F, font));
      int i = 0;
      float x = (float)((mc.getWindow().getScaledWidth() - this.getWidth()) * mc.getWindow().getScaleFactor() - 10.0);
      float y = 20.0F;
      float yOffset = 0.0F;

      for (ActiveMods.ModuleNode module : this.modules) {
         if (module.module.isEnabled()) {
            module.update(i, x, y, yOffset);
            yOffset += height[0] + 5.0F;
            i++;
         } else {
            module.update(-1, x, y, yOffset);
         }
      }

      for (ActiveMods.ModuleNode modulex : this.modules) {
         if (modulex.module.isEnabled() || !modulex.animationScale.isFinished()) {
            modulex.render();
         }
      }
   }

   public float getWidth() {
      return this.modules.get(0).getWidth() / (float)mc.getWindow().getScaleFactor();
   }

   public float getHeight() {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      float[] height = new float[1];
      nanoVGHelper.setupAndDraw(true, vg -> height[0] = nanoVGHelper.getTextHeight(vg, 20.0F, font));
      return ((float)this.modules.stream().filter(m -> m.getModule().isEnabled()).count() * (height[0] + 5.0F) + 10.0F)
         / (float)mc.getWindow().getScaleFactor();
   }

   @Generated
   public List<ActiveMods.ModuleNode> getModules() {
      return this.modules;
   }

   public static class ModuleNode {
      private final Module module;
      private final Animation animationScale = new Animation(Easing.EASE_OUT_EXPO, 500L);
      private final Animation animationY = new Animation(Easing.EASE_OUT_EXPO, 500L);
      private float x = 0.0F;
      private float y = 0.0F;
      private float width;
      private final ActiveMods parent;

      public ModuleNode(Module module, ActiveMods parent) {
         this.module = module;
         this.parent = parent;
      }

      public void update(int index, float x, float y, float yOffset) {
         this.x = x;
         this.y = y;
         this.animationScale.animate(index != -1 ? 1.0 : 0.0);
         this.animationY.animate(yOffset);
      }

      public void render() {
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         nanoVGHelper.setupAndDraw(
            true,
            vg -> {
               this.width = nanoVGHelper.getTextWidth(vg, this.module.getName(), 20.0F, ActiveMods.font);
               float height = nanoVGHelper.getTextHeight(vg, 20.0F, ActiveMods.font);
               float x = this.x + this.parent.getWidth() * (float)MinecraftAccessor.mc.getWindow().getScaleFactor() - this.width;
               float centerX = x + this.width / 2.0F;
               float realY = this.y + (float)this.animationY.getValue();
               float centerY = realY + height / 2.0F;
               if (this.module.isEnabled()) {
                  ActiveMods.drawTexture(vg, "shadow.png", x - 24.0F, realY - 24.0F, this.width + 64.0F, height + 48.0F, 0.57F);
               }

               nanoVGHelper.translate(vg, centerX, centerY);
               nanoVGHelper.scale(vg, (float)this.animationScale.getValue(), (float)this.animationScale.getValue());
               nanoVGHelper.translate(vg, -centerX, -centerY);
               nanoVGHelper.drawText(
                  vg,
                  this.module.getName(),
                  x,
                  realY,
                  new Color(1.0F, 1.0F, 1.0F, (float)this.animationScale.getValue() * 0.95F).getRGB(),
                  20.0F,
                  ActiveMods.font
               );
            }
         );
      }

      @Generated
      public Module getModule() {
         return this.module;
      }

      @Generated
      public Animation getAnimationScale() {
         return this.animationScale;
      }

      @Generated
      public Animation getAnimationY() {
         return this.animationY;
      }

      @Generated
      public float getX() {
         return this.x;
      }

      @Generated
      public float getY() {
         return this.y;
      }

      @Generated
      public float getWidth() {
         return this.width;
      }

      @Generated
      public ActiveMods getParent() {
         return this.parent;
      }
   }
}
