package dev.southside.gui.netease;

import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class ActionButton extends BasicElement {
   protected String text;
   private final int alignment;
   private float fontSize;
   private float cornerRadius;
   private final float xSpacing;
   private final float xPadding;
   public float x;
   public float y;
   public static final int ALIGNMENT_LEFT = 0;
   public static final int ALIGNMENT_CENTER = 2;
   public static final int ALIGNMENT_JUSTIFIED = 3;
   public static final int SIZE_32 = 32;
   public static final int SIZE_36 = 36;
   public static final int SIZE_40 = 40;
   public static final int SIZE_48 = 48;
   private boolean toggleable = false;
   private Runnable runnable;

   public ActionButton(int width, int size, int xSpacing, int xPadding, String text, int align, @NotNull ColorPalette colorPalette) {
      super(width, 32, colorPalette, true);
      if (text != null) {
         this.text = text;
      }

      this.colorPalette = colorPalette;
      this.alignment = align;
      this.cornerRadius = size == 48 ? 14.0F : 10.0F;
      this.xSpacing = xSpacing;
      this.xPadding = xPadding;
      this.height = size;
      this.fontSize = size == 48 ? 20.0F : size / 2 - 4;
   }

   public ActionButton(int width, int size, String text, int align, @NotNull ColorPalette colorPalette) {
      this(width, size, size == 48 ? 12 : 8, size != 36 && size != 40 ? (size == 48 ? 20 : 12) : 16, text, align, colorPalette);
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.x = x;
      this.y = y;
      this.update(x, y, inputHandler);
      float lastAlpha = nanoVGHelper.lastAlpha();
      if (this.disabled) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      float contentWidth = 0.0F;
      int color;
      if (this.colorPalette != ColorPalette.TERTIARY && this.colorPalette != ColorPalette.TERTIARY_DESTRUCTIVE) {
         nanoVGHelper.drawRoundedRect(vg, x, y, this.width, this.height, this.currentColor, this.cornerRadius);
         color = ColorUtils.setAlpha(Colors.WHITE, (int)(this.colorAnimation.getAlpha() * 255.0F));
      } else {
         color = this.currentColor;
      }

      float middle = x + this.width / 2.0F;
      float middleYText = y + this.height / 2.0F + this.fontSize / 8.0F;
      if (this.text != null) {
         contentWidth += nanoVGHelper.getTextWidth(vg, this.text, this.fontSize, Fonts.MEDIUM);
      }

      if (this.alignment == 2) {
         if (this.text != null) {
            nanoVGHelper.drawText(vg, this.text, middle - contentWidth / 2.0F, middleYText, color, this.fontSize, Fonts.MEDIUM);
         }
      } else if (this.alignment == 3) {
         if (this.text != null) {
            nanoVGHelper.drawText(vg, this.text, middle - contentWidth / 2.0F, middleYText, color, this.fontSize, Fonts.MEDIUM);
         }
      } else if (this.alignment == 0) {
         contentWidth = this.xPadding;
         if (this.text != null) {
            nanoVGHelper.drawText(vg, this.text, x + contentWidth, middleYText, color, this.fontSize, Fonts.MEDIUM);
         }
      }

      if (this.disabled) {
         nanoVGHelper.setAlpha(vg, lastAlpha);
      }
   }

   @Override
   public void onClick() {
      if (!this.disabled) {
         if (this.runnable != null) {
            this.runnable.run();
         }

         if (this.toggleable && this.toggled) {
            this.setColorPalette(ColorPalette.PRIMARY);
         } else if (this.toggleable) {
            this.setColorPalette(ColorPalette.SECONDARY);
         }
      }
   }

   @Override
   public void setToggled(boolean toggled) {
      this.toggled = toggled;
      if (toggled && this.toggleable) {
         this.setColorPalette(ColorPalette.PRIMARY);
      } else if (this.toggleable) {
         this.setColorPalette(ColorPalette.SECONDARY);
      }
   }

   public void setToggleable(boolean state) {
      this.toggleable = state;
   }

   public void setClickAction(Runnable runnable) {
      this.runnable = runnable;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public boolean hasClickAction() {
      return this.runnable != null;
   }

   @Generated
   public void setFontSize(float fontSize) {
      this.fontSize = fontSize;
   }

   @Generated
   public void setCornerRadius(float cornerRadius) {
      this.cornerRadius = cornerRadius;
   }

   @Generated
   public Runnable getRunnable() {
      return this.runnable;
   }
}
