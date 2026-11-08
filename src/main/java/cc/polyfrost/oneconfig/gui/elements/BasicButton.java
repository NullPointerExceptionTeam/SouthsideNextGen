package cc.polyfrost.oneconfig.gui.elements;

import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.pages.Page;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import org.jetbrains.annotations.NotNull;

public class BasicButton extends BasicElement {
   protected String text;
   protected SVG icon1;
   protected SVG icon2;
   private final int alignment;
   private final float fontSize;
   private final float cornerRadius;
   private final float xSpacing;
   private final float xPadding;
   private final int iconSize;
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
   private Page page;
   private Runnable runnable;

   public BasicButton(
      int width, int size, int iconSize, int xSpacing, int xPadding, String text, SVG icon1, SVG icon2, int align, @NotNull ColorPalette colorPalette
   ) {
      super(width, 32, colorPalette, true);
      if (text != null) {
         this.text = text;
      }

      if (icon1 != null) {
         this.icon1 = icon1;
      }

      if (icon2 != null) {
         this.icon2 = icon2;
      }

      this.colorPalette = colorPalette;
      this.alignment = align;
      this.cornerRadius = size == 48 ? 14.0F : 10.0F;
      this.xSpacing = xSpacing;
      this.xPadding = xPadding;
      this.height = size;
      this.iconSize = iconSize;
      this.fontSize = size == 48 ? 20.0F : size / 2 - 4;
   }

   public BasicButton(int width, int size, String text, SVG icon1, SVG icon2, int align, @NotNull ColorPalette colorPalette) {
      this(width, size, size / 2, size == 48 ? 12 : 8, size != 36 && size != 40 ? (size == 48 ? 20 : 12) : 16, text, icon1, icon2, align, colorPalette);
   }

   public BasicButton(int width, int size, SVG icon, int align, @NotNull ColorPalette colorPalette) {
      this(width, size, null, icon, null, align, colorPalette);
   }

   public BasicButton(int width, int size, String text, int align, @NotNull ColorPalette colorPalette) {
      this(width, size, text, null, null, align, colorPalette);
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.x = x;
      this.y = y;
      this.update(x, y, inputHandler);
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
      float middleYIcon = y + this.height / 2.0F - this.iconSize / 2.0F;
      float middleYText = y + this.height / 2.0F + this.fontSize / 8.0F;
      if (this.text != null) {
         contentWidth += nanoVGHelper.getTextWidth(vg, this.text, this.fontSize, Fonts.MEDIUM);
      }

      if (this.alignment == 2) {
         if (this.icon1 != null && this.icon2 == null && this.text == null) {
            this.drawIcon(vg, this.icon1, middle - this.iconSize / 2.0F, middleYIcon, this.iconSize, this.iconSize, color);
         } else {
            if (this.icon1 != null) {
               contentWidth += this.iconSize + this.xSpacing;
            }

            if (this.icon2 != null) {
               contentWidth += this.iconSize + this.xSpacing;
            }

            if (this.text != null) {
               nanoVGHelper.drawText(
                  vg,
                  this.text,
                  middle - contentWidth / 2.0F + (this.icon1 == null ? 0.0F : this.iconSize + this.xSpacing),
                  middleYText,
                  color,
                  this.fontSize,
                  Fonts.MEDIUM
               );
            }

            if (this.icon1 != null) {
               this.drawIcon(vg, this.icon1, middle - contentWidth / 2.0F, middleYIcon, this.iconSize, this.iconSize, color);
            }

            if (this.icon2 != null) {
               this.drawIcon(vg, this.icon2, middle + contentWidth / 2.0F - this.iconSize, middleYIcon, this.iconSize, this.iconSize, color);
            }
         }
      } else if (this.alignment == 3) {
         if (this.text != null) {
            nanoVGHelper.drawText(vg, this.text, middle - contentWidth / 2.0F, middleYText, color, this.fontSize, Fonts.MEDIUM);
         }

         if (this.icon1 != null) {
            this.drawIcon(vg, this.icon1, x + this.xPadding, middleYIcon, this.iconSize, this.iconSize, color);
         }

         if (this.icon2 != null) {
            this.drawIcon(vg, this.icon2, x + this.width - this.xPadding - this.iconSize, middleYIcon, this.iconSize, this.iconSize, color);
         }
      } else if (this.alignment == 0) {
         contentWidth = this.xPadding;
         if (this.icon1 != null) {
            this.drawIcon(vg, this.icon1, x + contentWidth, middleYIcon, this.iconSize, this.iconSize, color);
            contentWidth += this.iconSize + this.xSpacing;
         }

         if (this.text != null) {
            nanoVGHelper.drawText(vg, this.text, x + contentWidth, middleYText, color, this.fontSize, Fonts.MEDIUM);
         }

         if (this.icon2 != null) {
            this.drawIcon(vg, this.icon2, x + this.width - this.xPadding - this.iconSize, middleYIcon, this.iconSize, this.iconSize, color);
         }
      }

      if (this.disabled) {
         nanoVGHelper.setAlpha(vg, 1.0F);
      }
   }

   protected void drawIcon(long vg, SVG icon, float x, float y, float width, float height, int color) {
      NanoVGHelper.INSTANCE.drawSvg(vg, icon, x, y, width, height, color);
   }

   @Override
   public void onClick() {
      if (!this.disabled) {
         if (this.page != null && OneConfigGui.INSTANCE != null) {
            OneConfigGui.INSTANCE.openPage(this.page);
         } else if (this.runnable != null) {
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

   public void setClickAction(Page page) {
      this.page = page;
   }

   public void setClickAction(Runnable runnable) {
      this.runnable = runnable;
   }

   public Page getPage() {
      return this.page;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public void setLeftIcon(SVG icon) {
      this.icon1 = icon;
   }

   public void setRightIcon(SVG icon) {
      this.icon2 = icon;
   }

   public boolean hasClickAction() {
      return this.page != null || this.runnable != null;
   }
}
