package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutExpo;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import java.lang.reflect.Field;

public class ConfigDualOption extends BasicOption {
   private final String left;
   private final String right;
   private Animation posAnimation;

   public ConfigDualOption(
      Field field, Object parent, String name, String description, String category, String subcategory, int size, String left, String right
   ) {
      super(field, parent, name, description, category, subcategory, size);
      this.left = left;
      this.right = right;
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      boolean toggled = false;

      try {
         toggled = (Boolean)this.get();
         if (this.posAnimation == null) {
            this.posAnimation = new DummyAnimation(toggled ? 356.0F : 228.0F);
         }
      } catch (IllegalAccessException var12) {
      }

      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      boolean hoveredLeft = inputHandler.isAreaHovered(x + 226, y, 128.0F, 32.0F) && this.isEnabled();
      boolean hoveredRight = inputHandler.isAreaHovered(x + 354, y, 128.0F, 32.0F) && this.isEnabled();
      nanoVGHelper.drawText(vg, this.name, x, y + 16, this.nameColor, 14.0F, Fonts.MEDIUM);
      nanoVGHelper.drawRoundedRect(vg, x + 226, y, 256.0F, 32.0F, Colors.GRAY_600, 12.0F);
      nanoVGHelper.drawRoundedRect(vg, x + this.posAnimation.get(), y + 2, 124.0F, 28.0F, Colors.PRIMARY_600, 10.0F);
      if (!hoveredLeft && this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.8F);
      }

      nanoVGHelper.drawText(
         vg, this.left, x + 290 - nanoVGHelper.getTextWidth(vg, this.left, 12.0F, Fonts.MEDIUM) / 2.0F, y + 17, Colors.WHITE, 12.0F, Fonts.MEDIUM
      );
      if (this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 1.0F);
      }

      if (!hoveredRight && this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.8F);
      }

      nanoVGHelper.drawText(
         vg, this.right, x + 418 - nanoVGHelper.getTextWidth(vg, this.right, 12.0F, Fonts.MEDIUM) / 2.0F, y + 17, Colors.WHITE, 12.0F, Fonts.MEDIUM
      );
      nanoVGHelper.setAlpha(vg, 1.0F);
      if ((hoveredLeft && toggled || hoveredRight && !toggled) && inputHandler.isClicked()) {
         toggled = !toggled;

         try {
            this.set(toggled);
         } catch (IllegalAccessException var11) {
            System.err.println("failed to write config value: class=" + this + " fieldWatching=" + this.field + " valueWrite=" + toggled);
            var11.printStackTrace();
         }
      }

      if (toggled == this.posAnimation.isReversed()) {
         this.posAnimation = new EaseOutExpo(300, 228.0F, 356.0F, !toggled);
      }
   }
}
