package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuad;
import cc.polyfrost.oneconfig.gui.animations.EaseOutBump;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.lang.reflect.Field;

public class ConfigSwitch extends BasicOption {
   private ColorAnimation color;
   private Animation animation;

   public ConfigSwitch(Field field, Object parent, String name, String description, String category, String subcategory, int size) {
      super(field, parent, name, description, category, subcategory, size);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      boolean toggled = false;

      try {
         toggled = (Boolean)this.get();
         if (this.animation == null) {
            this.animation = new DummyAnimation(toggled ? 1.0F : 0.0F);
            this.color = new ColorAnimation(toggled ? ColorPalette.PRIMARY : ColorPalette.SECONDARY);
         }
      } catch (IllegalAccessException var13) {
      }

      float percentOn = this.animation.get();
      int x2 = x + 3 + (int)(percentOn * 18.0F);
      boolean hovered = inputHandler.isAreaHovered(x, y, 42.0F, 32.0F);
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      nanoVGHelper.drawRoundedRect(vg, x, y + 4, 42.0F, 24.0F, this.color.getColor(hovered, hovered && Platform.getMousePlatform().isButtonDown(0)), 12.0F);
      nanoVGHelper.drawRoundedRect(vg, x2, y + 7, 18.0F, 18.0F, Colors.WHITE, 9.0F);
      nanoVGHelper.drawText(vg, this.name, x + 50, y + 17, this.nameColor, 14.0F, Fonts.MEDIUM);
      if (inputHandler.isAreaClicked(x, y, 42.0F, 32.0F) && this.isEnabled()) {
         toggled = !toggled;

         try {
            this.set(toggled);
         } catch (IllegalAccessException var12) {
            System.err.println("failed to write config value: class=" + this + " fieldWatching=" + this.field + " valueWrite=" + toggled);
            var12.printStackTrace();
         }
      }

      if (toggled == this.animation.isReversed()) {
         if (Preferences.INSTANCE.toggleSwitchBounce.getValue()) {
            this.animation = new EaseOutBump(200, 0.0F, 1.0F, !toggled);
         } else {
            this.animation = new EaseInOutQuad(150, 0.0F, 1.0F, !toggled);
         }

         this.color.setPalette(toggled ? ColorPalette.PRIMARY : ColorPalette.SECONDARY);
      }

      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   @Override
   protected float getNameX(int x) {
      return x + 50;
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
