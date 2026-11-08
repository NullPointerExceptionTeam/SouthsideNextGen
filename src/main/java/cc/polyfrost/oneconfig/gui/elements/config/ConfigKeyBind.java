package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.BasicButton;
import cc.polyfrost.oneconfig.gui.elements.IFocusable;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.lang.reflect.Field;
import org.lwjgl.glfw.GLFW;

public class ConfigKeyBind extends BasicOption implements IFocusable {
   private final BasicButton button;
   private boolean clicked = false;
   private InputHandler inputHandler;
   private final boolean[] prevMouseState = new boolean[8];

   public ConfigKeyBind(Field field, Object parent, String name, String description, String category, String subcategory, int size) {
      super(field, parent, name, description, category, subcategory, size);
      this.button = new BasicButton(256, 32, "", SVGs.KEYSTROKE, null, 3, ColorPalette.SECONDARY);
      this.button.setToggleable(true);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.inputHandler = inputHandler;
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      nanoVGHelper.drawText(vg, this.name, x, y + 17, this.nameColor, 14.0F, Fonts.MEDIUM);
      OneKeyBind keyBind = this.getKeyBind();
      String text = keyBind.getDisplay();
      this.button.disable(!this.isEnabled());
      if (this.button.isToggled()) {
         inputHandler.blockAllInput();
         if (text.equals("")) {
            text = "Recording... (ESC to clear)";
         }

         if (!this.clicked) {
            keyBind.clearKeys();
            this.setKeyBind(keyBind);
            this.clicked = true;

            for (int b = 0; b < this.prevMouseState.length; b++) {
               this.prevMouseState[b] = GLFW.glfwGetMouseButton(UMinecraft.getWindowHandle(), b) == 1;
            }
         } else if (keyBind.getSize() != 0 && !keyBind.isActive()) {
            this.button.setToggled(false);
            this.clicked = false;
            OneConfigGui.INSTANCE.allowClose = true;
            inputHandler.stopBlockingInput();
         } else {
            OneConfigGui.INSTANCE.allowClose = false;
            this.pollMouseButtons(keyBind);
         }
      } else if (text.equals("")) {
         text = "NONE";
      }

      this.button.setText(text);
      this.button.draw(vg, x + (this.size == 1 ? 224 : 736), y, inputHandler);
      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   private void pollMouseButtons(OneKeyBind keyBind) {
      long handle = UMinecraft.getWindowHandle();

      for (int b = 2; b < this.prevMouseState.length; b++) {
         boolean nowPressed = GLFW.glfwGetMouseButton(handle, b) == 1;
         if (nowPressed && !this.prevMouseState[b]) {
            keyBind.addKey(b, true);
            this.setKeyBind(keyBind);
         }

         this.prevMouseState[b] = nowPressed;
      }
   }

   @Override
   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      if (this.button.isToggled()) {
         OneKeyBind keyBind = this.getKeyBind();
         if (keyCode == 256) {
            keyBind.clearKeys();
            this.button.setToggled(false);
            OneConfigGui.INSTANCE.allowClose = true;
            this.clicked = false;
            this.inputHandler.stopBlockingInput();
         } else {
            keyBind.addKey(keyCode);
         }

         this.setKeyBind(keyBind);
      }
   }

   private OneKeyBind getKeyBind() {
      OneKeyBind keyBind = new OneKeyBind();

      try {
         this.field.setAccessible(true);
         keyBind = (OneKeyBind)this.get();
      } catch (IllegalAccessException var3) {
      }

      return keyBind;
   }

   private void setKeyBind(OneKeyBind keyBind) {
      try {
         this.set(keyBind);
      } catch (IllegalAccessException var3) {
      }
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   public boolean hasFocus() {
      return this.clicked;
   }
}
