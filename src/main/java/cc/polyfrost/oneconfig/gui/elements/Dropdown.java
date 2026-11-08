package cc.polyfrost.oneconfig.gui.elements;

import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutQuad;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Dropdown extends BasicButton {
   private final LinkedHashMap<String, ColorAnimation> opts;
   private int selected;
   private float optsWidth = -1.0F;
   private float rotation;
   private final float xPadding;
   private final float fontSize;
   private EaseOutQuad anim = null;
   private InputHandler inputHandler;

   public Dropdown(int width, int size, int xPadding, String[] opts, int selected, ColorPalette palette) {
      super(width, size, (size - 8) / 2, 8, xPadding, opts[selected], null, SVGs.CHEVRON_DOWN, 0, palette);
      this.xPadding = xPadding;
      this.selected = selected;
      this.opts = new LinkedHashMap<>(opts.length);

      for (String opt : opts) {
         this.opts.put(opt, new ColorAnimation(palette == ColorPalette.TERTIARY ? ColorPalette.SECONDARY : palette));
      }

      this.fontSize = size == 48 ? 20.0F : size / 2 - 4;
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.inputHandler = inputHandler;
      if (this.optsWidth == -1.0F) {
         for (String s : this.opts.keySet()) {
            this.optsWidth = Math.max(this.optsWidth, nanoVGHelper.getTextWidth(vg, s, this.fontSize, Fonts.REGULAR));
         }

         this.optsWidth = Math.max(this.optsWidth, (float)this.width);
         this.width = (int)this.optsWidth;
      }

      if (this.toggled) {
         inputHandler.stopBlockingInput();
      }

      if (this.opts.size() != 1 && this.anim != null) {
         float val = this.anim.get();
         this.rotation = val / ((this.opts.size() - 1) * this.fontSize * 2.0F) * 180.0F;
         nanoVGHelper.drawRoundedRect(
            vg,
            x,
            y + this.height,
            this.optsWidth,
            val,
            this.colorPalette == ColorPalette.TERTIARY ? ColorPalette.SECONDARY.getNormalColor() : this.currentColor,
            8.0F
         );
         if (this.anim.isFinished() && !this.anim.isReversed() && this.toggled) {
            float i = y + this.height;
            int index = 0;

            for (Entry<String, ColorAnimation> entry : this.opts.entrySet()) {
               String s = entry.getKey();
               ColorAnimation anim = entry.getValue();
               if (index == this.selected) {
                  index++;
               } else {
                  boolean hovered = inputHandler.isAreaHovered(x, i, this.optsWidth, 24.0F);
                  boolean clicked = hovered && inputHandler.isClicked(true);
                  nanoVGHelper.drawRoundedRect(vg, x, i, this.optsWidth, this.fontSize * 2.0F, anim.getColor(hovered, clicked), 8.0F);
                  nanoVGHelper.drawText(vg, s, x + this.xPadding, i + this.fontSize, Colors.WHITE_90, this.fontSize, Fonts.REGULAR);
                  if (clicked) {
                     this.selected = index;
                     this.onChange(this.selected);
                     super.text = s;
                     this.toggled = false;
                  }

                  index++;
                  i += this.fontSize * 2.0F;
               }
            }
         }
      }

      super.draw(vg, x, y, inputHandler);
      if (this.toggled) {
         inputHandler.blockAllInput();
      }
   }

   @Override
   public void setToggled(boolean toggled) {
      super.setToggled(toggled);
      if (!toggled) {
         this.inputHandler.blockAllInput();
      }
   }

   @Override
   public void drawIcon(long vg, SVG icon, float x, float y, float width, float height, int color) {
      if (this.rotation == 0.0F) {
         super.drawIcon(vg, icon, x, y, width, height, color);
      } else {
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         float xx = x + width / 2.0F;
         float yy = y + height / 2.0F;
         nanoVGHelper.translate(vg, xx, yy);
         nanoVGHelper.rotate(vg, this.rotation);
         nanoVGHelper.drawSvg(vg, icon, -(width / 2.0F), -(height / 2.0F), width, height, color);
         nanoVGHelper.rotate(vg, -this.rotation);
         nanoVGHelper.translate(vg, -xx, -yy);
      }
   }

   @Override
   public void update(float x, float y, InputHandler inputHandler) {
      super.update(x, y, inputHandler);
      if (inputHandler.isClicked() && !inputHandler.isAreaHovered(x, y, this.optsWidth, this.height) && this.toggled) {
         this.toggled = false;
         inputHandler.stopBlockingInput();
      }

      if (this.toggled && this.anim == null) {
         this.anim = new EaseOutQuad(200, 0.0F, (this.opts.size() - 1) * this.fontSize * 2.0F, false);
      }

      if (this.anim != null && this.anim.isFinished() && this.anim.isReversed()) {
         this.anim = null;
      }

      if (!this.toggled && this.anim != null && !this.anim.isReversed()) {
         this.anim = new EaseOutQuad(200, 0.0F, (this.opts.size() - 1) * this.fontSize * 2.0F, true);
      }
   }

   public int getSelected() {
      return this.selected;
   }

   public void select(int index) {
      this.selected = index;
      super.text = (String)this.opts.keySet().toArray()[index];
      this.onChange(index);
   }

   public void onChange(int changedTo) {
   }
}
