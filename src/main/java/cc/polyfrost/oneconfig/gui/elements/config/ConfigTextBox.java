package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.elements.IFocusable;
import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import java.lang.reflect.Field;

public class ConfigTextBox extends BasicOption implements IFocusable {
   private final boolean secure;
   private final boolean multiLine;
   public final TextInputField textField;

   public ConfigTextBox(
      Field field,
      Object parent,
      String name,
      String description,
      String category,
      String subcategory,
      int size,
      String placeholder,
      boolean secure,
      boolean multiLine
   ) {
      super(field, parent, name, description, category, subcategory, size);
      this.secure = secure;
      this.multiLine = multiLine;
      this.textField = new TextInputField(size == 1 ? 256 : 640, multiLine ? 64 : 32, placeholder, multiLine, secure, SVGs.TEXT_INPUT, 12.0F);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      this.textField.disable(!this.isEnabled());
      nanoVGHelper.drawText(vg, this.name, x, y + 16, this.nameColor, 14.0F, Fonts.MEDIUM);

      try {
         String value = (String)this.get();
         this.textField.setInput(value == null ? "" : value);
      } catch (IllegalAccessException var10) {
      }

      if (this.multiLine && this.textField.getLines() > 2) {
         this.textField.setHeight(64 + 24 * (this.textField.getLines() - 2));
      } else if (this.multiLine) {
         this.textField.setHeight(64);
      }

      this.textField.draw(vg, x + (this.size == 1 ? 224 : 352), y, inputHandler);
      if (this.secure) {
         SVG icon = this.textField.getPassword() ? SVGs.EYE_OFF : SVGs.EYE;
         boolean hovered = inputHandler.isAreaHovered(x + 967, y + 7, 18.0F, 18.0F) && this.isEnabled();
         int color = hovered ? Colors.WHITE : Colors.WHITE_80;
         if (hovered && inputHandler.isClicked()) {
            this.textField.setPassword(!this.textField.getPassword());
         }

         if (hovered && Platform.getMousePlatform().isButtonDown(0)) {
            nanoVGHelper.setAlpha(vg, 0.5F);
         }

         nanoVGHelper.drawSvg(vg, icon, (float)(x + 967), (float)(y + 7), 18.0F, 18.0F, color);
      }

      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   @Override
   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      if (this.isEnabled()) {
         this.textField.keyTyped(keyCode, scanCode, modifiers);

         try {
            this.set(this.textField.getInput());
         } catch (IllegalAccessException var5) {
         }
      }
   }

   @Override
   public void charTyped(char chr, int modifiers) {
      if (this.isEnabled()) {
         this.textField.charTyped(chr, modifiers);

         try {
            this.set(this.textField.getInput());
         } catch (IllegalAccessException var4) {
         }
      }
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
      if (this.isEnabled()) {
         this.textField.mouseClicked(mouseX, mouseY, mouseButton);

         try {
            this.set(this.textField.getInput());
         } catch (IllegalAccessException var7) {
         }
      }
   }

   @Override
   public void mouseReleased(double mouseX, double mouseY, int state) {
      if (this.isEnabled()) {
         this.textField.mouseReleased(mouseX, mouseY, state);

         try {
            this.set(this.textField.getInput());
         } catch (IllegalAccessException var7) {
         }
      }
   }

   @Override
   public int getHeight() {
      return this.multiLine ? this.textField.getHeight() : 32;
   }

   @Override
   protected boolean shouldDrawDescription() {
      return super.shouldDrawDescription() && !this.textField.isToggled();
   }

   @Override
   public boolean hasFocus() {
      return this.textField.isToggled();
   }
}
