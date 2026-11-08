package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.elements.BasicButton;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ConfigButton extends BasicOption {
   private final BasicButton button;

   public ConfigButton(Runnable runnable, Object parent, String name, String description, String category, String subcategory, int size, String text) {
      super(null, parent, name, description, category, subcategory, size);
      this.button = new BasicButton(size == 1 ? 128 : 256, 32, text, 2, ColorPalette.PRIMARY);
      this.button.setClickAction(runnable);
   }

   public ConfigButton(Field field, Object parent, String name, String description, String category, String subcategory, int size, String text) {
      super(field, parent, name, description, category, subcategory, size);
      this.button = new BasicButton(size == 1 ? 128 : 256, 32, text, 2, ColorPalette.PRIMARY);
      this.button.setClickAction(getRunnableFromField(field, parent));
   }

   public ConfigButton(Method method, Object parent, String name, String description, String category, String subcategory, int size, String text) {
      super(null, parent, name, description, category, subcategory, size);
      this.button = new BasicButton(size == 1 ? 128 : 256, 32, text, 2, ColorPalette.PRIMARY);
      this.button
         .setClickAction(
            () -> {
               try {
                  method.invoke(parent);
               } catch (Exception var3) {
                  if (var3.getMessage() != null && var3.getMessage().contains("wrong number of arguments")) {
                     throw new IllegalArgumentException(
                        "Button method "
                           + method.getDeclaringClass().getName()
                           + "."
                           + method.getName()
                           + "("
                           + Arrays.toString((Object[])method.getGenericParameterTypes())
                           + ") must take no arguments!"
                     );
                  }

                  var3.printStackTrace();
               }
            }
         );
   }

   private static Runnable getRunnableFromField(Field field, Object parent) {
      Runnable runnable = () -> {};

      try {
         runnable = (Runnable)field.get(parent);
      } catch (IllegalAccessException var4) {
         var4.printStackTrace();
      }

      return runnable;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      this.button.disable(!this.isEnabled());
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      nanoVGHelper.drawText(vg, this.name, x, y + 17, this.nameColor, 14.0F, Fonts.MEDIUM);
      this.button.draw(vg, x + (this.size == 1 ? 352 : 736), y, inputHandler);
      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
