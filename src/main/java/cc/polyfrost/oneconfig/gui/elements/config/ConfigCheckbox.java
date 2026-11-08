package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuad;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import java.awt.Color;
import java.lang.reflect.Field;

public class ConfigCheckbox extends BasicOption {
   private final ColorAnimation color = new ColorAnimation(ColorPalette.SECONDARY);
   private Animation animation;

   public ConfigCheckbox(Field field, Object parent, String name, String description, String category, String subcategory, int size) {
      super(field, parent, name, description, category, subcategory, size);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      boolean toggled = false;

      try {
         toggled = (Boolean)this.get();
         if (this.animation == null) {
            this.animation = new DummyAnimation(toggled ? 1.0F : 0.0F);
         }
      } catch (IllegalAccessException var12) {
      }

      boolean hover = inputHandler.isAreaHovered(x, y + 4, 24.0F, 24.0F);
      boolean clicked = inputHandler.isClicked() && hover;
      if (clicked && this.isEnabled()) {
         toggled = !toggled;

         try {
            this.set(toggled);
         } catch (IllegalAccessException var11) {
            System.err.println("failed to write config value: class=" + this + " fieldWatching=" + this.field + " valueWrite=" + toggled);
            var11.printStackTrace();
         }
      }

      if (toggled == this.animation.isReversed()) {
         this.animation = new EaseInOutQuad(100, 0.0F, 1.0F, !toggled);
      }

      float percentOn = this.animation.get();
      nanoVGHelper.drawText(vg, this.name, x + 32, y + 17, this.nameColor, 14.0F, Fonts.MEDIUM);
      nanoVGHelper.drawRoundedRect(vg, x, y + 4, 24.0F, 24.0F, this.color.getColor(hover, hover && Platform.getMousePlatform().isButtonDown(0)), 6.0F);
      nanoVGHelper.drawHollowRoundRect(vg, x, y + 4, 23.5F, 23.5F, Colors.GRAY_300, 6.0F, 1.0F);
      nanoVGHelper.drawRoundedRect(vg, x, y + 4, 24.0F, 24.0F, ColorUtils.setAlpha(Colors.PRIMARY_500, (int)(percentOn * 255.0F)), 6.0F);
      nanoVGHelper.drawSvg(vg, SVGs.CHECKBOX_TICK, (float)x, (float)(y + 4), 24.0F, 24.0F, new Color(1.0F, 1.0F, 1.0F, percentOn).getRGB());
      if (toggled && hover) {
         nanoVGHelper.drawHollowRoundRect(vg, x - 1, y + 3, 24.0F, 24.0F, Colors.PRIMARY_600, 6.0F, 2.0F);
      }

      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   @Override
   protected float getNameX(int x) {
      return x + 32;
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
