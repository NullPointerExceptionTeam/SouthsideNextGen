package cc.polyfrost.oneconfig.gui.elements;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.Images;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.libs.universal.UMouse;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class ColorSelector {
   private OneColor color;
   private static final BasicButton closeBtn = new BasicButton(32, 32, SVGs.X_CLOSE, 2, ColorPalette.TERTIARY_DESTRUCTIVE);
   private final BasicButton faveBtn = new BasicButton(32, 32, SVGs.HEART_OUTLINE, 2, ColorPalette.SECONDARY);
   private final BasicButton recentBtn = new BasicButton(32, 32, SVGs.HISTORY, 2, ColorPalette.SECONDARY) {
      @Override
      public void onClick() {
         super.onClick();
         if (!ColorSelector.this.recentColors.get(0).getColor().equals(ColorSelector.this.color)) {
            ColorSelector.assignRecentColor(ColorSelector.this.color.clone());
            ColorSelector.this.recentColors.remove(5);
            ColorSelector.this.recentColors.add(0, new ColorSelector.ColorBox(ColorSelector.this.color.clone()));
         }
      }
   };
   private static final BasicButton pickerBtn = new BasicButton(32, 32, SVGs.PICKER, 2, ColorPalette.TERTIARY);
   public final Dropdown modeDropdown = new Dropdown(128, 36, 18, new String[]{"Solid Color", "Chroma"}, 0, ColorPalette.TERTIARY) {
      @Override
      public void onChange(int changedTo) {
         switch (changedTo) {
            case 0:
            default:
               ColorSelector.this.picker = ColorSelector.this.new SolidColorPicker();
               break;
            case 1:
               ColorSelector.this.picker = ColorSelector.this.new ChromaColorPicker();
         }
      }
   };
   public final Dropdown inputTypeDropdown = new Dropdown(90, 36, 20, new String[]{"HEX", "HSBA"}, 0, ColorPalette.TERTIARY) {
      @Override
      public void onChange(int changedTo) {
         switch (changedTo) {
            case 0:
            default:
               ColorSelector.this.colorInput = ColorSelector.this.new HexInput();
               break;
            case 1:
               ColorSelector.this.colorInput = ColorSelector.this.new HSBAInput();
         }
      }
   };
   private final ArrayList<ColorSelector.ColorBox> favoriteColors = new ArrayList<>(6);
   private final ArrayList<ColorSelector.ColorBox> recentColors = new ArrayList<>(6);
   private float x;
   private float y;
   private float dragX;
   private float dragY;
   @NotNull
   private ColorSelector.ColorInput colorInput;
   @NotNull
   private ColorSelector.ColorPicker picker;
   private static float width = 296.0F;
   private static float height = 398.0F;
   private boolean pickerIsActive;
   private boolean mouseWasDown;
   private boolean dragging;
   private final boolean hasAlpha;
   private Scissor inputScissor;
   private final InputHandler inputHandler;

   public ColorSelector(OneColor color, float mouseX, float mouseY, boolean hasAlpha, InputHandler inputHandler) {
      this.inputHandler = inputHandler;

      for (OneColor oo : OneConfigConfig.recentColors) {
         this.recentColors.add(new ColorSelector.ColorBox(oo));
      }

      for (OneColor o : OneConfigConfig.favoriteColors) {
         this.favoriteColors.add(new ColorSelector.ColorBox(o));
      }

      while (this.favoriteColors.size() < 6) {
         this.favoriteColors.add(new ColorSelector.ColorBox(new OneColor(0, 0, 0, 0)));
      }

      while (this.recentColors.size() < 6) {
         this.recentColors.add(new ColorSelector.ColorBox(new OneColor(0, 0, 0, 0)));
      }

      this.color = color;
      this.hasAlpha = hasAlpha;
      if (color.getDataBit() != -1) {
         this.modeDropdown.select(1);
      } else {
         this.picker = new ColorSelector.SolidColorPicker();
      }

      this.x = mouseX - width / 2.0F;
      this.y = Math.max(0.0F, mouseY - height);
      this.colorInput = new ColorSelector.HexInput();
   }

   public void draw(long vg) {
      if (this.inputScissor != null) {
         this.inputHandler.stopBlock(this.inputScissor);
      }

      if (pickerBtn.toggled) {
         this.inputHandler.blockAllInput();
         int color = NanoVGHelper.INSTANCE.readPixels((int)UMouse.Raw.getX(), UResolution.getViewportHeight() - (int)UMouse.Raw.getY(), 1, 1)[0];
         NanoVGHelper.INSTANCE.drawRoundedRect(vg, this.inputHandler.mouseX() - 16.0F, this.inputHandler.mouseY() - 33.0F, 32.0F, 32.0F, -1, 16.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(vg, this.inputHandler.mouseX() - 15.0F, this.inputHandler.mouseY() - 32.0F, 30.0F, 30.0F, color, 15.0F);
         if (this.inputHandler.isClicked(true)) {
            this.__setColor(new OneColor(color));
            pickerBtn.toggled = false;
            this.inputHandler.stopBlockingInput();
         }
      } else {
         Scissor scissor = ScissorHelper.INSTANCE.scissor(vg, this.x - 3.0F, this.y - 3.0F, width + 6.0F, height + 6.0F);
         NanoVGHelper.INSTANCE.drawHollowRoundRect(vg, this.x - 3.0F, this.y - 3.0F, width + 3.0F, height + 3.0F, Colors.GRAY_700, 20.0F, 3.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(vg, this.x, this.y, width, height, Colors.GRAY_800, 20.0F);
         closeBtn.draw(vg, this.x + 248.0F + 8.0F, this.y + 12.0F, this.inputHandler);
         this.picker.drawAndUpdate(vg, this.x + 16.0F, this.y + 48.0F, this.inputHandler);
         if (this.modeDropdown.getSelected() == 1) {
            this.y += 20.0F;
            this.inputHandler.stopBlockingInput();
         }

         this.colorInput.drawAndUpdate(vg, this.x + 100.0F, this.y + 260.0F, this.inputHandler);
         pickerBtn.draw(vg, this.x + 252.0F, this.y + 260.0F, this.inputHandler);
         this.faveBtn.draw(vg, this.x + 12.0F, this.y + 304.0F, this.inputHandler);
         if (this.faveBtn.isClicked()) {
            this.favoriteColors.remove(5);
            this.favoriteColors.add(0, new ColorSelector.ColorBox(this.color.clone()));
         }

         this.recentBtn.draw(vg, this.x + 12.0F, this.y + 348.0F, this.inputHandler);

         for (int i = 0; i < 6; i++) {
            ColorSelector.ColorBox box = this.favoriteColors.get(i);
            box.draw(vg, this.x + 52.0F + i * 40, this.y + 304.0F, this.inputHandler);
            if (box.isClicked()) {
               this.__setColor(box.getColor().clone());
            }

            ColorSelector.ColorBox rBox = this.recentColors.get(i);
            rBox.draw(vg, this.x + 52.0F + i * 40, this.y + 348.0F, this.inputHandler);
            if (rBox.isClicked()) {
               this.__setColor(rBox.getColor().clone());
            }
         }

         this.inputTypeDropdown.draw(vg, this.x - 2.0F, this.y + 260.0F, this.inputHandler);
         if (this.modeDropdown.getSelected() == 1) {
            this.y -= 20.0F;
         }

         this.modeDropdown.draw(vg, this.x - 2.0F, this.y + 8.0F, this.inputHandler);
         boolean hovered = Platform.getMousePlatform().isButtonDown(0) && this.inputHandler.isAreaHovered(this.x, this.y, width, 48.0F);
         if (hovered && Platform.getMousePlatform().isButtonDown(0) && !this.mouseWasDown) {
            this.dragging = true;
            this.dragX = this.inputHandler.mouseX() - this.x;
            this.dragY = this.inputHandler.mouseY() - this.y;
         }

         if (this.dragging) {
            this.x = this.inputHandler.mouseX() - this.dragX;
            this.y = this.inputHandler.mouseY() - this.dragY;
         }

         this.inputScissor = this.inputHandler.blockInputArea(this.x, this.y, width, height);
         if (closeBtn.isClicked() && !this.pickerIsActive) {
            OneConfigGui.INSTANCE.closeColorSelector();
         }

         ScissorHelper.INSTANCE.resetScissor(vg, scissor);
         if (this.dragging && this.inputHandler.isClicked(true)) {
            this.dragging = false;
         }

         if (this.pickerIsActive && this.inputHandler.isClicked(true)) {
            this.pickerIsActive = false;
         }

         this.mouseWasDown = Platform.getMousePlatform().isButtonDown(0);
      }
   }

   public OneColor getColor() {
      return this.color;
   }

   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      this.colorInput.keyTyped(keyCode, scanCode, modifiers);
   }

   public void charTyped(char chr, int modifiers) {
      this.colorInput.charTyped(chr, modifiers);
   }

   public void onClose() {
      this.inputHandler.stopBlockingInput();
      int i = 0;

      for (ColorSelector.ColorBox box : this.favoriteColors) {
         if (!box.isEmpty()) {
            assignFavoriteColor(box.getColor().clone(), i);
            i++;
         }
      }

      assignRecentColor(this.color.clone());
      OneConfigConfig.getInstance().save();
   }

   public static void assignRecentColor(OneColor color) {
      if (OneConfigConfig.recentColors.size() == 6) {
         OneConfigConfig.recentColors.remove(5);
         OneConfigConfig.recentColors.add(0, color);
      } else {
         OneConfigConfig.recentColors.add(color);
      }
   }

   public static void assignFavoriteColor(OneColor color, int index) {
      if (index < 0) {
         index = 0;
      }

      if (index > 5) {
         index = 5;
      }

      if (index >= OneConfigConfig.favoriteColors.size()) {
         OneConfigConfig.favoriteColors.add(color);
      } else {
         OneConfigConfig.favoriteColors.set(index, color);
      }
   }

   private void __setColor(OneColor color) {
      if (color.getRGB() != 0) {
         this.color = color;
         this.picker.onColorChanged();
         this.colorInput.onColorChanged();
      }
   }

   public boolean isAlphaAllowed() {
      return this.hasAlpha;
   }

   private final class ChromaColorPicker implements ColorSelector.ColorPicker {
      private float cursorX;
      private float cursorY;
      private final ColorSelector.ColorSlider alphaSlider;
      public final Slider speedSlider = new Slider(200, 1.0F, 30.0F, 1.0F, 2);

      public ChromaColorPicker() {
         ColorSelector.width = 296.0F;
         ColorSelector.height = 408.0F;
         this.cursorX = ColorSelector.this.color.getSaturation() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getBrightness() / 100.0F) * 200.0F;
         this.alphaSlider = new ColorSelector.ColorSlider(
            200, 0.0F, 255.0F, 255 - ColorSelector.this.color.getAlpha(), Colors.TRANSPARENT, ColorSelector.this.color.getRGBMax(true)
         );
         if (ColorSelector.this.color.getDataBit() == -1) {
            ColorSelector.this.color.setChromaSpeed(30);
         }

         this.speedSlider.setValueInverted(ColorSelector.this.color.getDataBit());
         if (!ColorSelector.this.hasAlpha) {
            this.alphaSlider.disable(true);
         }
      }

      @Override
      public void drawAndUpdate(long vg, float x, float y, InputHandler inputHandler) {
         this.speedSlider.draw(vg, x, y, inputHandler);
         y += 20.0F;
         NanoVGHelper.INSTANCE.drawHSBBox(vg, x, y, 200.0F, 200.0F, ColorSelector.this.color.getRGBMax(true));
         this.drawCursor(vg, x + this.cursorX, y + this.cursorY, ColorSelector.this.color.getRGB());
         this.alphaSlider.draw(vg, x + 216.0F, y, inputHandler);
         this.alphaSlider.setGradient(ColorSelector.this.color.getRGBNoAlpha(), Colors.TRANSPARENT);
         if (this.alphaSlider.isDragging() || this.speedSlider.isDragging()) {
            ColorSelector.this.color
               .setHSBA(
                  ColorSelector.this.color.getHue(),
                  ColorSelector.this.color.getSaturation(),
                  ColorSelector.this.color.getBrightness(),
                  (int)this.alphaSlider.getValueInverted()
               );
            ColorSelector.this.color.setChromaSpeed(Math.round(this.speedSlider.getValueInverted()));
         }

         if (!ColorSelector.this.colorInput.isEditing()) {
            ColorSelector.this.colorInput.onColorChanged();
         }

         boolean hovered = Platform.getMousePlatform().isButtonDown(0) && inputHandler.isAreaHovered(x, y, 200.0F, 200.0F);
         if (hovered && Platform.getMousePlatform().isButtonDown(0) && !ColorSelector.this.mouseWasDown) {
            ColorSelector.this.pickerIsActive = true;
         }

         if (ColorSelector.this.pickerIsActive && !ColorSelector.this.colorInput.isEditing()) {
            this.cursorX = inputHandler.mouseX() - x;
            this.cursorY = inputHandler.mouseY() - y;
            if (this.cursorX < 0.0F) {
               this.cursorX = 0.0F;
            }

            if (this.cursorY < 0.0F) {
               this.cursorY = 0.0F;
            }

            if (this.cursorX > 200.0F) {
               this.cursorX = 200.0F;
            }

            if (this.cursorY > 200.0F) {
               this.cursorY = 200.0F;
            }

            float progressX = this.cursorX / 200.0F;
            float progressY = Math.abs(this.cursorY / 200.0F - 1.0F);
            ColorSelector.this.color
               .setHSBA(
                  ColorSelector.this.color.getHue(), Math.round(progressX * 100.0F), Math.round(progressY * 100.0F), (int)this.alphaSlider.getValueInverted()
               );
         }
      }

      @Override
      public void onColorChanged() {
         this.cursorX = ColorSelector.this.color.getSaturation() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getBrightness() / 100.0F) * 200.0F;
         this.alphaSlider.setValueInverted(ColorSelector.this.color.getAlpha());
         this.alphaSlider.setGradient(ColorSelector.this.color.getRGBNoAlpha(), Colors.TRANSPARENT);
         this.speedSlider.setValueInverted(ColorSelector.this.color.getDataBit());
      }
   }

   static final class ColorBox extends BasicElement {
      private OneColor color;

      public ColorBox(OneColor color) {
         super(
            32,
            32,
            new ColorPalette(color.getRGB(), color.toJavaColor().brighter().getRGB(), ColorUtils.setAlpha(color.getRGB(), (int)(color.getAlpha() * 0.8F))),
            true
         );
         this.color = color;
      }

      @Override
      public void draw(long vg, float x, float y, InputHandler inputHandler) {
         NanoVGHelper.INSTANCE.drawRoundedRect(vg, x, y, 32.0F, 32.0F, Colors.GRAY_900, 8.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(vg, x, y, 32.0F, 32.0F, this.color.getRGB(), 8.0F);
         super.update(x, y, inputHandler);
      }

      public OneColor getColor() {
         return this.color;
      }

      public void setColor(OneColor color) {
         this.color = color;
      }

      public boolean isEmpty() {
         return this.color.getRGB() == 0;
      }
   }

   interface ColorInput {
      void drawAndUpdate(long var1, float var3, float var4, InputHandler var5);

      void onColorChanged();

      void keyTyped(int var1, int var2, int var3);

      boolean isEditing();

      void charTyped(char var1, int var2);
   }

   interface ColorPicker {
      void drawAndUpdate(long var1, float var3, float var4, InputHandler var5);

      void onColorChanged();

      default void drawCursor(long vg, float cursorX, float cursorY, int color) {
         NanoVGHelper.INSTANCE.drawRoundedRect(vg, cursorX - 7.0F, cursorY - 7.0F, 14.0F, 14.0F, Colors.WHITE, 14.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(vg, cursorX - 5.0F, cursorY - 5.0F, 10.0F, 10.0F, color, 10.0F);
      }
   }

   static final class ColorSlider extends Slider {
      private int gradColorStart;
      private int gradColorEnd;
      private final boolean img;

      public ColorSlider(int length, float min, float max, float startValue, int gradColorStart, int gradColorEnd) {
         super(length, min, max, startValue, 1);
         super.width = 16;
         super.dragPointerSize = this.width;
         if (gradColorEnd == 0 && gradColorStart == 0) {
            this.gradColorStart = 0;
            this.gradColorEnd = 0;
            this.img = true;
         } else {
            this.gradColorStart = gradColorStart;
            this.gradColorEnd = gradColorEnd;
            this.img = false;
         }
      }

      public ColorSlider(int length, float min, float max, float startValue) {
         this(length, min, max, startValue, 0, 0);
      }

      @Override
      public void draw(long vg, float x, float y, InputHandler inputHandler) {
         if (!this.disabled) {
            super.update(x, y, inputHandler);
         } else {
            NanoVGHelper.INSTANCE.setAlpha(vg, 0.5F);
         }

         if (this.img) {
            NanoVGHelper.INSTANCE.drawRoundImage(vg, Images.HUE_GRADIENT, x, y, (float)this.width, (float)this.height, 8.0F);
         } else {
            NanoVGHelper.INSTANCE.drawRoundImage(vg, Images.VERTICAL_ALPHA_GRID, x + 1.0F, y + 1.0F, (float)(this.width - 2), (float)(this.height - 2), 8.0F);
            NanoVGHelper.INSTANCE
               .drawGradientRoundedRect(vg, x, y, this.width, this.height, this.gradColorStart, this.gradColorEnd, 8.0F, NanoVGHelper.GradientDirection.DOWN);
         }

         NanoVGHelper.INSTANCE.drawHollowRoundRect(vg, x, this.currentDragPoint, this.width - 1, this.width - 1, Colors.WHITE, 8.0F, 1.0F);
         NanoVGHelper.INSTANCE.setAlpha(vg, 1.0F);
      }

      public void setGradient(int gradColorStart, int gradColorEnd) {
         if (!this.img) {
            this.gradColorEnd = gradColorEnd;
            this.gradColorStart = gradColorStart;
         }
      }
   }

   final class HSBAInput implements ColorSelector.ColorInput {
      final TextInputField[] inputs = new TextInputField[4];
      private boolean editing = false;

      HSBAInput() {
         for (int i = 0; i < this.inputs.length; i++) {
            final int index = i;
            this.inputs[i] = new TextInputField(40, 32, true, 4.0F) {
               @Override
               public void onClose() {
                  HSBAInput.this.inputs[index].setInput(ColorSelector.this.color.getHSBA()[index] + "");
               }
            };
            this.inputs[i].setBoarderThickness(1.0F);
            this.inputs[i].setInput(ColorSelector.this.color.getHSBA()[i] + "");
         }

         if (!ColorSelector.this.hasAlpha) {
            this.inputs[3].disable(true);
         }
      }

      @Override
      public void drawAndUpdate(long vg, float x, float y, InputHandler inputHandler) {
         this.editing = false;

         for (int i = 0; i < 4; i++) {
            TextInputField in = this.inputs[i];
            in.setErrored(false);
            in.draw(vg, x - 12.0F + 42 * i, y, inputHandler);
            if (in.isToggled()) {
               this.editing = true;
               ColorSelector.this.color.setHSBA(i, this.parseIntOrElse(in.getInput(), ColorSelector.this.color.getHSBA()[i]));
               ColorSelector.this.picker.onColorChanged();
            }
         }
      }

      @Override
      public void onColorChanged() {
         this.inputs[0].setInput(ColorSelector.this.color.getHue() + "");
         this.inputs[1].setInput(ColorSelector.this.color.getSaturation() + "");
         this.inputs[2].setInput(ColorSelector.this.color.getBrightness() + "");
         this.inputs[3].setInput(ColorSelector.this.color.getAlpha() + "");
      }

      @Override
      public void keyTyped(int keyCode, int scanCode, int modifiers) {
         for (TextInputField i : this.inputs) {
            i.keyTyped(keyCode, scanCode, modifiers);
         }
      }

      @Override
      public void charTyped(char chr, int modifiers) {
         for (TextInputField i : this.inputs) {
            i.charTyped(chr, modifiers);
         }
      }

      @Override
      public boolean isEditing() {
         return this.editing;
      }

      int parseIntOrElse(String s, int orElse) {
         try {
            return Integer.parseInt(s);
         } catch (Exception var4) {
            return orElse;
         }
      }
   }

   final class HexInput implements ColorSelector.ColorInput {
      private final TextInputField alphaInput = new TextInputField(56, 32, true, 4.0F) {
         @Override
         public void onClose() {
            HexInput.this.alphaInput.setInput(Math.round(ColorSelector.this.color.getAlpha() / 2.55F) + "%");
         }
      };
      private final TextInputField hexInput = new TextInputField(80, 32, true, 4.0F) {
         @Override
         public void onClose() {
            HexInput.this.hexInput.setInput("#" + ColorSelector.this.color.getHex());
         }
      };
      private boolean editing = false;

      public HexInput() {
         this.hexInput.setBoarderThickness(1.0F);
         this.alphaInput.setBoarderThickness(1.0F);
         this.hexInput.setInput("#" + ColorSelector.this.color.getHex());
         this.alphaInput.setInput(Math.round(ColorSelector.this.color.getAlpha() / 2.55F) + "%");
         if (!ColorSelector.this.hasAlpha) {
            this.alphaInput.disable(true);
         }
      }

      @Override
      public void drawAndUpdate(long vg, float x, float y, InputHandler inputHandler) {
         this.hexInput.draw(vg, x, y, inputHandler);
         this.alphaInput.draw(vg, x + 88.0F, y, inputHandler);
         this.hexInput.setErrored(false);
         this.alphaInput.setErrored(false);
         this.editing = false;
         if (this.hexInput.isToggled()) {
            this.editing = true;

            try {
               ColorSelector.this.color.setColorFromHex(this.hexInput.getInput());
               ColorSelector.this.picker.onColorChanged();
            } catch (Exception var9) {
               this.hexInput.setErrored(true);
            }
         }

         if (this.alphaInput.isToggled()) {
            this.editing = true;
            String s = this.alphaInput.getInput().endsWith("%")
               ? this.alphaInput.getInput().substring(0, this.alphaInput.getInput().length() - 1)
               : this.alphaInput.getInput();

            try {
               ColorSelector.this.color.setAlpha(Math.round(Math.max(0.0F, Math.min(Float.parseFloat(s), 100.0F)) * 2.55F));
               ColorSelector.this.picker.onColorChanged();
            } catch (Exception var8) {
               this.alphaInput.setErrored(true);
            }
         }
      }

      @Override
      public void onColorChanged() {
         this.hexInput.setInput("#" + ColorSelector.this.color.getHex());
         this.alphaInput.setInput(Math.round(ColorSelector.this.color.getAlpha() / 2.55F) + "%");
      }

      @Override
      public void keyTyped(int keyCode, int scanCode, int modifiers) {
         this.hexInput.keyTyped(keyCode, scanCode, modifiers);
         this.alphaInput.keyTyped(keyCode, scanCode, modifiers);
      }

      @Override
      public void charTyped(char chr, int modifiers) {
         this.hexInput.charTyped(chr, modifiers);
         this.alphaInput.charTyped(chr, modifiers);
      }

      @Override
      public boolean isEditing() {
         return this.editing;
      }
   }

   private final class SolidColorPicker implements ColorSelector.ColorPicker {
      private float cursorX;
      private float cursorY;
      private final ColorSelector.ColorSlider alphaSlider;
      private final ColorSelector.ColorSlider hueSlider;

      public SolidColorPicker() {
         ColorSelector.width = 296.0F;
         ColorSelector.height = 398.0F;
         this.cursorX = ColorSelector.this.color.getSaturation() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getBrightness() / 100.0F) * 200.0F;
         this.alphaSlider = new ColorSelector.ColorSlider(
            200, 0.0F, 255.0F, 255 - ColorSelector.this.color.getAlpha(), Colors.TRANSPARENT, ColorSelector.this.color.getRGBMax(true)
         );
         this.hueSlider = new ColorSelector.ColorSlider(200, 0.0F, 360.0F, 360 - ColorSelector.this.color.getHue());
         ColorSelector.this.color.setChromaSpeed(-1);
         if (!ColorSelector.this.hasAlpha) {
            this.alphaSlider.disable(true);
         }
      }

      @Override
      public void drawAndUpdate(long vg, float x, float y, InputHandler inputHandler) {
         NanoVGHelper.INSTANCE.drawHSBBox(vg, x, y, 200.0F, 200.0F, ColorSelector.this.color.getRGBMax(true));
         this.drawCursor(vg, x + this.cursorX, y + this.cursorY, ColorSelector.this.color.getRGB());
         this.hueSlider.draw(vg, x + 216.0F, y, inputHandler);
         this.alphaSlider.draw(vg, x + 248.0F, y, inputHandler);
         this.alphaSlider.setGradient(ColorSelector.this.color.getRGBNoAlpha(), Colors.TRANSPARENT);
         if (this.hueSlider.isDragging() || this.alphaSlider.isDragging()) {
            ColorSelector.this.color
               .setHSBA(
                  (int)this.hueSlider.getValueInverted(),
                  ColorSelector.this.color.getSaturation(),
                  ColorSelector.this.color.getBrightness(),
                  (int)this.alphaSlider.getValueInverted()
               );
            ColorSelector.this.colorInput.onColorChanged();
         }

         boolean hovered = Platform.getMousePlatform().isButtonDown(0) && inputHandler.isAreaHovered(x, y, 200.0F, 200.0F);
         if (hovered && Platform.getMousePlatform().isButtonDown(0) && !ColorSelector.this.mouseWasDown) {
            ColorSelector.this.pickerIsActive = true;
         }

         if (ColorSelector.this.pickerIsActive) {
            this.cursorX = inputHandler.mouseX() - x;
            this.cursorY = inputHandler.mouseY() - y;
            if (this.cursorX < 0.0F) {
               this.cursorX = 0.0F;
            }

            if (this.cursorY < 0.0F) {
               this.cursorY = 0.0F;
            }

            if (this.cursorX > 200.0F) {
               this.cursorX = 200.0F;
            }

            if (this.cursorY > 200.0F) {
               this.cursorY = 200.0F;
            }

            float progressX = this.cursorX / 200.0F;
            float progressY = Math.abs(this.cursorY / 200.0F - 1.0F);
            ColorSelector.this.color
               .setHSBA(
                  (int)this.hueSlider.getValueInverted(),
                  Math.round(progressX * 100.0F),
                  Math.round(progressY * 100.0F),
                  (int)this.alphaSlider.getValueInverted()
               );
         }
      }

      @Override
      public void onColorChanged() {
         this.cursorX = ColorSelector.this.color.getSaturation() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getBrightness() / 100.0F) * 200.0F;
         this.alphaSlider.setValueInverted(ColorSelector.this.color.getAlpha());
         this.hueSlider.setValueInverted(ColorSelector.this.color.getHue());
         this.alphaSlider.setGradient(ColorSelector.this.color.getRGBNoAlpha(), Colors.TRANSPARENT);
      }
   }
}
