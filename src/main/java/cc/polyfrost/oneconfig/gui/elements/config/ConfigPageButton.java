package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.pages.ModConfigPage;
import cc.polyfrost.oneconfig.gui.pages.Page;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.lang.reflect.Field;

public class ConfigPageButton extends BasicOption {
   public final Page page;
   public final String description;
   private final ColorAnimation backgroundColor = new ColorAnimation(ColorPalette.SECONDARY);

   public ConfigPageButton(Field field, Object parent, String name, String description, String category, String subcategory, OptionPage page) {
      super(field, parent, name, "", category, subcategory, 2);
      this.description = description;
      this.page = new ModConfigPage(page);
   }

   public ConfigPageButton(Field field, Object parent, String name, String description, String category, String subcategory, Page page) {
      super(field, parent, name, "", category, subcategory, 2);
      this.description = description;
      this.page = page;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      int height = this.description.equals("") ? 64 : 96;
      boolean hovered = inputHandler.isAreaHovered(x - 16, y, 1024.0F, height) && this.isEnabled();
      boolean clicked = hovered && inputHandler.isClicked();
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      nanoVGHelper.drawRoundedRect(
         vg, x - 16, y, 1024.0F, height, this.backgroundColor.getColor(hovered, hovered && Platform.getMousePlatform().isButtonDown(0)), 20.0F
      );
      nanoVGHelper.drawText(vg, this.name, x + 10, y + 32, Colors.WHITE_90, 24.0F, Fonts.MEDIUM);
      if (!this.description.equals("")) {
         nanoVGHelper.drawText(vg, this.description, x + 10, y + 70, Colors.WHITE_90, 14.0F, Fonts.MEDIUM);
      }

      nanoVGHelper.drawSvg(vg, SVGs.CARET_RIGHT, x + 981.0F, y + (this.description.equals("") ? 20.0F : 36.0F), 13.0F, 22.0F);
      if (clicked) {
         OneConfigGui.INSTANCE.openPage(this.page);
      }

      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   @Override
   public int getHeight() {
      return this.description.equals("") ? 64 : 96;
   }
}
