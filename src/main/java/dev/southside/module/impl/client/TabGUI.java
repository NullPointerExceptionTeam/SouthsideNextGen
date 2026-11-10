package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.asset.NVGAsset;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.Southside;
import dev.southside.event.events.KeyPressEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.render.jello.ColorUtils;
import dev.southside.utils.render.jello.PartialTicksAnim;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;

public class TabGUI extends Module {
   public TabGUI.JelloTabGUI tabgui;
   public static float scale = 2.0F;

   public TabGUI() {
      super(Category.Client, "TabGUI", "Jello tab gui");
   }

   @Override
   public void onEnable() {
      this.tabgui = new TabGUI.JelloTabGUI();
   }

   @EventHandler
   public void onUpdateEvent(PlayerUpdateEvent event) {
      this.tabgui.blur.tick();
      this.tabgui.modulesBlur.tick();

      for (TabGUI.JelloTabGUI.CategoryNode c : this.tabgui.cats) {
         boolean selected = c.equals(this.tabgui.cats.get(this.tabgui.currentCategory));
         c.anim.interpolate(selected ? 7.0F : 0.0F, -1.0);
      }

      this.tabgui.anim.interpolate(this.tabgui.currentCategory * 15, 3.0);
      int y = 0;
      int categoryIndex = this.tabgui.currentCategory;
      TabGUI.JelloTabGUI.CategoryNode category = this.tabgui.cats.get(categoryIndex);
      category.anim2.interpolate(category.selectedIndex * 15.0F, 3.0);

      for (Module module : Southside.INSTANCE.getModuleManager().getModulesByCategory(category.category)) {
         TabGUI.JelloTabGUI.ModuleNode m = this.tabgui.mods.get(module);
         if (y == category.selectedIndex) {
            m.sAnim.interpolate(5.0F, 3.0);
         } else {
            m.sAnim.interpolate(0.0F, 3.0);
         }

         y++;
      }
   }

   @EventHandler
   public void onKeyEvent(KeyPressEvent event) {
      if (event.getKey() == 265) {
         this.tabgui.keyUp();
      }

      if (event.getKey() == 262) {
         this.tabgui.keyRight();
      }

      if (event.getKey() == 264) {
         this.tabgui.keyDown();
      }

      if (event.getKey() == 263) {
         this.tabgui.keyLeft();
      }
   }

   void drawImage(long ng, NVGAsset image, float x, float y, float u, float v, float w, float h, float uw, float uh) {
      float texW = image.getWidth();
      float texH = image.getHeight();
      float u0 = u / texW;
      float v0 = v / texH;
      float u1 = (u + uw) / texW;
      float v1 = (v + uh) / texH;
      float us = u1 - u0;
      float vs = v1 - v0;
      NVGPaint imagePaint = NVGPaint.calloc();
      NanoVG.nvgImagePattern(ng, x, y, w, h, 0.0F, image.getImage(), 1.0F, imagePaint);
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

   private void drawTexture(long vg, String image, float x, float y, float u, float v, float w, float h, float uw, float uh) {
      NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      AtomicBoolean loaded = new AtomicBoolean(false);
      String imagePath = "/assets/southside/textures/gui/hud/tabgui/" + image;
      nanovg.setupAndDraw(vg1 -> loaded.set(assetHelper.loadImage(vg1, imagePath, TabGUI.class)));
      if (loaded.get()) {
         NVGAsset asset = assetHelper.getNVGImage(imagePath);
         this.drawImage(vg, asset, x, y, u, v, w, h, uw, uh);
      }
   }

   private void drawTexture(long nvg, String image, float x, float y, float w, float h) {
      NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      AtomicBoolean loaded = new AtomicBoolean(false);
      String imagePath = "/assets/southside/textures/gui/hud/tabgui/" + image;
      nanovg.setupAndDraw(vg1 -> loaded.set(assetHelper.loadImage(vg1, imagePath, TabGUI.class)));
      if (loaded.get()) {
         NVGAsset asset = assetHelper.getNVGImage(imagePath);
         float texW = asset.getWidth();
         float texH = asset.getHeight();
         this.drawImage(nvg, asset, x, y, 0.0F, 0.0F, w, h, texW, texH);
      }
   }

   @EventHandler
   public void onRenderEvent(Render2DEvent e) {
      int categoryIndex = this.tabgui.currentCategory;
      TabGUI.JelloTabGUI.CategoryNode category = this.tabgui.cats.get(categoryIndex);
      List<Module> modules = Southside.INSTANCE.getModuleManager().getModulesByCategory(category.category);
      NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
      nanovg.setupAndDraw(
         nvg -> {
            this.tabgui.blur.update(scale * 5.0F, scale * 50.0F, scale * 75.0F, scale * (this.tabgui.cats.size() * 15));
            nanovg.drawDropShadow(
               nvg, scale * 5.0F, scale * 50.0F, scale * 75.0F, scale * (this.tabgui.cats.size() * 15), 4.0F, 4.0F, 0.0F, new Color(0, 0, 0, 30)
            );
            this.tabgui.blur.render(nvg, e.getRenderTick());
            this.tabgui.seenTrans = this.tabgui.anim.getValue();
            this.drawTexture(nvg, "slector.png", scale * 5.0F, scale * (50.0F + this.tabgui.seenTrans), scale * 75.0F, scale * 15.0F);
            int x = 0;

            for (TabGUI.JelloTabGUI.CategoryNode c : this.tabgui.cats) {
               c.seenTrans = c.anim.getValue();
               nanovg.drawText(
                  nvg, c.category.name(), scale * (5.0F + c.seenTrans + 5.5F), scale * (50 + x * 15 + 5 + 3), Color.WHITE.getRGB(), 20.0F, Fonts.JELLOLIGHT
               );
               x++;
            }

            if (this.tabgui.showModules) {
               int size = modules.size();
               float smoothTrans = category.anim2.getValue();
               int y = 0;
               if (size != 0) {
                  this.tabgui.modulesBlur.update(scale * 85.5F, scale * 50.0F, scale * 84.0F, scale * 15.0F * modules.size());
                  nanovg.drawDropShadow(
                     nvg, scale * 85.5F, scale * 50.0F, scale * 84.0F, scale * 15.0F * modules.size(), 4.0F, 4.0F, 0.0F, new Color(0, 0, 0, 30)
                  );
                  this.tabgui.modulesBlur.render(nvg, e.getRenderTick());
                  this.drawTexture(nvg, "slector.png", scale * 85.0F, scale * (50.0F + smoothTrans), scale * 85.0F, scale * 15.0F);
               }

               for (Module module : modules) {
                  TabGUI.JelloTabGUI.ModuleNode m = this.tabgui.mods.get(module);
                  nanovg.drawText(
                     nvg,
                     module.getName(),
                     scale * (90.5F + m.sAnim.getValue()),
                     scale * (50 + 15 * y + 5 + 3),
                     -1,
                     20.0F,
                     module.enabled.getValue() ? Fonts.JELLOMEDIUM : Fonts.JELLOLIGHT
                  );
                  y++;
               }
            }
         }
      );
   }

   public static class DickBlur {
      public int tRed;
      public int tGreen;
      public int tBlue;
      public int lasttRed;
      public int lasttGreen;
      public int lasttBlue;
      public int bRed;
      public int bGreen;
      public int bBlue;
      public int lastbRed;
      public int lastbGreen;
      public int lastbBlue;
      private int x;
      private int y;
      private int width;
      private int height;

      public void update(float x, float y, float width, float height) {
         this.x = (int)x;
         this.y = (int)y;
         this.width = (int)width;
         this.height = (int)height;
      }

      public void tick() {
         this.lasttRed = this.tRed;
         this.lasttGreen = this.tGreen;
         this.lasttBlue = this.tBlue;
         this.lastbRed = this.bRed;
         this.lastbGreen = this.bGreen;
         this.lastbBlue = this.bBlue;
         this.tRed = Math.min(this.tRed, 255);
         this.tGreen = Math.min(this.tGreen, 255);
         this.tBlue = Math.min(this.tBlue, 255);
         this.tRed = Math.max(this.tRed, 0);
         this.tGreen = Math.max(this.tGreen, 0);
         this.tBlue = Math.max(this.tBlue, 0);
         this.bRed = Math.min(this.bRed, 255);
         this.bGreen = Math.min(this.bGreen, 255);
         this.bBlue = Math.min(this.bBlue, 255);
         this.bRed = Math.max(this.bRed, 0);
         this.bGreen = Math.max(this.bGreen, 0);
         this.bBlue = Math.max(this.bBlue, 0);
      }

      public void render(long vg, float tickDelta) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         if (!(TabGUI.mc.currentScreen instanceof ChatScreen)) {
            int rX = Math.min(this.x, TabGUI.mc.getWindow().getFramebufferWidth());
            int rY = Math.min(this.y, TabGUI.mc.getWindow().getFramebufferHeight());
            int rX1 = Math.min(rX + this.width, TabGUI.mc.getWindow().getFramebufferWidth());
            int rY1 = Math.min(rY + this.height, TabGUI.mc.getWindow().getFramebufferHeight());
            int rWidth = rX1 - rX;
            int rHeight = rY1 - rY;
            IntBuffer pixelBuffer = BufferUtils.createIntBuffer(rWidth * rHeight);
            GL11.glPixelStorei(3333, 1);
            GL11.glPixelStorei(3317, 1);
            GL11.glReadPixels(rX, TabGUI.mc.getWindow().getFramebufferHeight() - rHeight - rY, rWidth, rHeight, 32993, 33639, pixelBuffer);
            int colorTop = pixelBuffer.get(rWidth * rHeight - 1);
            int colorTopRight = pixelBuffer.get(rWidth * (rHeight - 1));
            int colorBottomRight = pixelBuffer.get(rWidth - 1);
            int colorBottomLeft = pixelBuffer.get(0);
            pixelBuffer.clear();
            Color top = ColorUtils.blend(new Color(colorTop), new Color(colorTopRight));
            Color bottom = ColorUtils.blend(new Color(colorBottomLeft), new Color(colorBottomRight));
            this.bRed = (int)(this.bRed + ((bottom.getRed() - this.bRed) / 5 + 0.1));
            this.bGreen = (int)(this.bGreen + ((bottom.getGreen() - this.bGreen) / 5 + 0.1));
            this.bBlue = (int)(this.bBlue + ((bottom.getBlue() - this.bBlue) / 5 + 0.1));
            this.tRed = (int)(this.tRed + ((top.getRed() - this.tRed) / 5 + 0.1));
            this.tGreen = (int)(this.tGreen + ((top.getGreen() - this.tGreen) / 5 + 0.1));
            this.tBlue = (int)(this.tBlue + ((top.getBlue() - this.tBlue) / 5 + 0.1));
         }

         int tR = this.smoothAnimation(this.tRed, this.lasttRed, tickDelta);
         int tG = this.smoothAnimation(this.tGreen, this.lasttGreen, tickDelta);
         int tB = this.smoothAnimation(this.tBlue, this.lasttBlue, tickDelta);
         int bR = this.smoothAnimation(this.bRed, this.lastbRed, tickDelta);
         int bG = this.smoothAnimation(this.bGreen, this.lastbGreen, tickDelta);
         int bB = this.smoothAnimation(this.bBlue, this.lastbBlue, tickDelta);
         nvg.drawGradientRect(
            vg,
            this.x,
            this.y,
            this.width,
            this.height,
            new Color(tR, tG, tB, 255).getRGB(),
            new Color(bR, bG, bB, 255).getRGB(),
            NanoVGHelper.GradientDirection.DOWN
         );
      }

      public int smoothAnimation(double current, double last, float tickDelta) {
         return (int)(current * tickDelta + last * (1.0F - tickDelta));
      }
   }

   public static class JelloTabGUI {
      public int currentCategory;
      public boolean viewingCats = true;
      public float seenTrans = 0.0F;
      public boolean showModules;
      public PartialTicksAnim anim = new PartialTicksAnim(0.0F);
      public final TabGUI.DickBlur blur = new TabGUI.DickBlur();
      public final TabGUI.DickBlur modulesBlur = new TabGUI.DickBlur();
      public List<TabGUI.JelloTabGUI.CategoryNode> cats = new java.util.ArrayList<TabGUI.JelloTabGUI.CategoryNode>() {
         {
            this.add(new TabGUI.JelloTabGUI.CategoryNode(Category.Combat, 0));
            this.add(new TabGUI.JelloTabGUI.CategoryNode(Category.Misc, 1));
            this.add(new TabGUI.JelloTabGUI.CategoryNode(Category.Movement, 2));
            this.add(new TabGUI.JelloTabGUI.CategoryNode(Category.Player, 3));
            this.add(new TabGUI.JelloTabGUI.CategoryNode(Category.World, 4));
         }
      };
      public Map<Module, TabGUI.JelloTabGUI.ModuleNode> mods = new HashMap<Module, TabGUI.JelloTabGUI.ModuleNode>() {
         {
            for (Module module : Southside.INSTANCE.getModuleManager().getModules()) {
               this.put(module, new TabGUI.JelloTabGUI.ModuleNode());
            }
         }
      };

      public void keyRight() {
         if (this.viewingCats) {
            if (!this.showModules) {
               this.showModules = true;
               int categoryIndex = this.currentCategory;
               TabGUI.JelloTabGUI.CategoryNode category = this.cats.get(categoryIndex);
               category.selectedIndex = 0;
            } else if (!Southside.INSTANCE.getModuleManager().getModulesByCategory(this.cats.get(this.currentCategory).category).isEmpty()) {
               Southside.INSTANCE
                  .getModuleManager()
                  .getModulesByCategory(this.cats.get(this.currentCategory).category)
                  .get(this.cats.get(this.currentCategory).selectedIndex)
                  .toggle();
            }
         }
      }

      public void keyLeft() {
         if (this.viewingCats && this.showModules) {
            this.showModules = false;
         }
      }

      public void keyDown() {
         if (this.viewingCats) {
            if (!this.showModules) {
               if (this.currentCategory < this.cats.size() - 1) {
                  this.currentCategory++;
               } else {
                  this.currentCategory = 0;
               }
            } else if (this.cats.get(this.currentCategory).selectedIndex
               < Southside.INSTANCE.getModuleManager().getModulesByCategory(this.cats.get(this.currentCategory).category).size() - 1) {
               this.cats.get(this.currentCategory).selectedIndex++;
            } else {
               this.cats.get(this.currentCategory).selectedIndex = 0;
            }
         }
      }

      public void keyUp() {
         if (this.viewingCats) {
            if (!this.showModules) {
               if (this.currentCategory > 0) {
                  this.currentCategory--;
               } else {
                  this.currentCategory = this.cats.size() - 1;
               }
            } else if (this.cats.get(this.currentCategory).selectedIndex > 0) {
               this.cats.get(this.currentCategory).selectedIndex--;
            } else {
               this.cats.get(this.currentCategory).selectedIndex = Southside.INSTANCE
                     .getModuleManager()
                     .getModulesByCategory(this.cats.get(this.currentCategory).category)
                     .size()
                  - 1;
            }
         }
      }

      public static class CategoryNode {
         public final Category category;
         public final int index;
         public float seenTrans;
         public PartialTicksAnim anim = new PartialTicksAnim(0.0F);
         public int selectedIndex;
         public PartialTicksAnim anim2 = new PartialTicksAnim(0.0F);

         public CategoryNode(Category category, int index) {
            this.category = category;
            this.index = index;
         }
      }

      public static class ModuleNode {
         public PartialTicksAnim sAnim = new PartialTicksAnim(0.0F);
      }
   }
}
