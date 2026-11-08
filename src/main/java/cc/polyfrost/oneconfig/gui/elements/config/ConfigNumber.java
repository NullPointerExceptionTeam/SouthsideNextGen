package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.elements.IFocusable;
import cc.polyfrost.oneconfig.gui.elements.text.NumberInputField;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import com.google.gson.internal.LazilyParsedNumber;
import java.lang.reflect.Field;

public class ConfigNumber extends BasicOption implements IFocusable {
   private final NumberInputField inputField;
   private boolean isFloat = true;
   private final int size;

   public ConfigNumber(
      Field field, Object parent, String name, String description, String category, String subcategory, float min, float max, int step, int size
   ) {
      super(field, parent, name, description, category, subcategory, size);
      this.inputField = new NumberInputField(84, 32, 0.0F, min, max, step);
      this.size = size;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      float value = 0.0F;
      this.inputField.disable(!this.isEnabled());
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      if (this.inputField.isToggled() || this.inputField.arrowsClicked()) {
         value = this.inputField.getCurrentValue();
      }

      if (this.inputField.isToggled() || this.inputField.arrowsClicked()) {
         this.setValue(value);
      }

      if (!this.inputField.isToggled()) {
         try {
            Object object = this.get();
            this.isFloat = true;

            try {
               if (object instanceof LazilyParsedNumber lazilyParsedNumber) {
                  value = lazilyParsedNumber.floatValue();
               } else if (object instanceof Double doubleValue) {
                  value = doubleValue.floatValue();
               } else if (object instanceof Integer integer) {
                  value = integer.floatValue();
               } else if (this.isFloat) {
                  value = (Float)object;
               } else {
                  value = ((Integer)object).intValue();
               }
            } catch (ClassCastException var12) {
               System.err.println("?");
            }
         } catch (IllegalAccessException var13) {
         }
      }

      if (!this.inputField.isToggled()) {
         this.inputField.setCurrentValue(value);
      }

      nanoVGHelper.drawText(vg, this.name, x, y + 17, this.nameColor, 14.0F, Fonts.MEDIUM);
      this.inputField.draw(vg, x + (this.size == 1 ? 396 : 892), y, inputHandler);
      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   private void setValue(float value) {
      try {
         if (this.isFloat) {
            this.set(value);
         } else {
            this.set(Math.round(value));
         }
      } catch (IllegalAccessException var3) {
      }
   }

   @Override
   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      this.inputField.keyTyped(keyCode, scanCode, modifiers);
   }

   @Override
   public void charTyped(char chr, int modifiers) {
      this.inputField.charTyped(chr, modifiers);
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   protected boolean shouldDrawDescription() {
      return super.shouldDrawDescription() && !this.inputField.isToggled();
   }

   @Override
   public boolean hasFocus() {
      return this.inputField.isToggled();
   }
}
