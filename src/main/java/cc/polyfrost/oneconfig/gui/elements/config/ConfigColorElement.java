package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.gui.elements.ColorSelector;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.Images;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.RenderTickDelay;
import dev.southside.utils.ChatUtils;
import java.lang.reflect.Field;

public class ConfigColorElement extends BasicOption {
   private final BasicElement element = new BasicElement(64, 32, false);
   private final boolean allowAlpha;
   private ColorSelector colorSelector;
   private boolean open = false;

   public ConfigColorElement(Field field, Object parent, String name, String description, String category, String subcategory, int size, boolean allowAlpha) {
      super(field, parent, name, description, category, subcategory, size);
      this.allowAlpha = allowAlpha;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      if (OneConfigGui.INSTANCE != null) {
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         if (!this.isEnabled()) {
            nanoVGHelper.setAlpha(vg, 0.5F);
         }

         this.element.disable(!this.isEnabled());
         int x1 = this.size == 1 ? x : x + 512;

         OneColor color;
         try {
            color = (OneColor)this.get();
         } catch (IllegalAccessException var11) {
            return;
         }

         nanoVGHelper.drawText(vg, this.name, x, y + 16, this.nameColor, 14.0F, Fonts.MEDIUM);
         this.element.update(x1 + 416, y, inputHandler);
         nanoVGHelper.drawHollowRoundRect(vg, x1 + 415, y - 1, 64.0F, 32.0F, Colors.GRAY_300, 12.0F, 2.0F);
         nanoVGHelper.drawRoundImage(vg, Images.ALPHA_GRID.filePath, x1 + 420, y + 4, 56.0F, 24.0F, 8.0F, this.getClass());
         nanoVGHelper.drawRoundedRect(vg, x1 + 420, y + 4, 56.0F, 24.0F, color.getRGB(), 8.0F);
         if (this.element.isClicked() && !this.open) {
            OneColor finalColor = new OneColor(color.getRGB());
            new RenderTickDelay(() -> {
               this.open = true;
               this.colorSelector = new ColorSelector(finalColor, inputHandler.mouseX(), inputHandler.mouseY(), this.allowAlpha, inputHandler);
               OneConfigGui.INSTANCE.initColorSelector(this.colorSelector);
            }, 1);
         }

         if (OneConfigGui.INSTANCE.currentColorSelector != this.colorSelector) {
            this.open = false;
         } else if (this.open) {
            try {
               if (!OneConfigGui.INSTANCE.getColor().toJavaColor().equals(((OneColor)this.field.get(this.parent)).toJavaColor())) {
                  ((OneColor)this.field.get(this.parent)).setFromOneColor(OneConfigGui.INSTANCE.getColor());
                  this.triggerListeners();
               }
            } catch (IllegalAccessException var10) {
            }
         }

         nanoVGHelper.setAlpha(vg, 1.0F);
      }
   }

   protected void setColor(OneColor color) {
      try {
         this.set(color);
      } catch (IllegalAccessException var3) {
         ChatUtils.info("?");
      }
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
