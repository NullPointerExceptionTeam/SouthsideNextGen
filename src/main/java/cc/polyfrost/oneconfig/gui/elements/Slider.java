package cc.polyfrost.oneconfig.gui.elements;

import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;

public class Slider extends BasicElement {
   public static final int VERTICAL = 1;
   public static final int HORIZONTAL = 2;
   private final float min;
   private final float max;
   protected float value;
   protected float currentDragPoint;
   protected float dragPointerSize = 8.0F;
   private boolean dragging = false;
   private final int mode;
   private boolean mouseWasDown = false;

   public Slider(int length, float min, float max, float startValue, int mode) {
      super(mode == 2 ? length : 8, mode == 2 ? 8 : length, false);
      this.min = min;
      this.max = max;
      this.mode = mode;
      this.setValue(startValue);
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      if (!this.disabled) {
         this.update(x, y, inputHandler);
      } else {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      if (this.dragging) {
         inputHandler.stopBlockingInput();
      }

      if (this.mode == 2) {
         nanoVGHelper.drawRoundedRect(vg, x, y + 2.0F, this.width, this.height - 4, Colors.GRAY_300, 3.0F);
         nanoVGHelper.drawRoundedRect(vg, x, y + 2.0F, this.width * this.value, this.height - 4, Colors.PRIMARY_500, 3.0F);
         nanoVGHelper.drawRoundedRect(vg, this.currentDragPoint - this.dragPointerSize / 2.0F, y - 8.0F + 6.0F, 12.0F, 12.0F, Colors.WHITE, 12.0F);
      } else {
         nanoVGHelper.drawRoundedRect(vg, x + 2.0F, y, this.width - 4, this.height, Colors.GRAY_300, 3.0F);
         nanoVGHelper.drawRoundedRect(
            vg, x + 2.0F, y + this.height - this.height * this.value, this.width - 4, this.height * this.value, Colors.PRIMARY_500, 3.0F
         );
         nanoVGHelper.drawRoundedRect(vg, x - 8.0F + 6.0F, this.currentDragPoint - this.dragPointerSize / 2.0F, 12.0F, 12.0F, Colors.WHITE, 12.0F);
      }

      nanoVGHelper.setAlpha(vg, 1.0F);
      if (this.dragging) {
         inputHandler.blockAllInput();
      }
   }

   @Override
   public void update(float x, float y, InputHandler inputHandler) {
      super.update(x, y, inputHandler);
      boolean isMouseDown = Platform.getMousePlatform().isButtonDown(0);
      boolean hovered;
      if (this.mode == 2) {
         hovered = inputHandler.isAreaHovered(x - 6.0F, y - 3.0F, this.width + 12, this.height + 6);
      } else {
         hovered = inputHandler.isAreaHovered(x - 3.0F, y - 6.0F, this.width + 6, this.height + 12);
      }

      if (hovered && isMouseDown && !this.mouseWasDown) {
         this.dragging = true;
      }

      this.mouseWasDown = isMouseDown;
      if (this.dragging) {
         if (this.mode == 2) {
            this.value = (inputHandler.mouseX() - x) / this.width;
         } else {
            this.value = (inputHandler.mouseY() - y) / this.height;
         }
      }

      if (this.dragging && inputHandler.isClicked(true)) {
         this.dragging = false;
         inputHandler.stopBlockingInput();
         if (this.mode == 2) {
            this.value = (inputHandler.mouseX() - x) / this.width;
         } else {
            this.value = (inputHandler.mouseY() - y) / this.height;
         }
      }

      if (this.value < 0.0F) {
         this.value = 0.0F;
      }

      if (this.value > 1.0F) {
         this.value = 1.0F;
      }

      if (this.mode == 2) {
         this.currentDragPoint = x + (this.width - this.dragPointerSize) * this.value;
      } else {
         this.currentDragPoint = y + (this.height - this.dragPointerSize) * this.value;
      }
   }

   public static float invert(float max, float value) {
      return max - value;
   }

   public float getValueInverted() {
      return invert(1.0F, this.value) * (this.max - this.min) + this.min;
   }

   public float getValue() {
      return this.value * (this.max - this.min) + this.min;
   }

   public void setValue(float value) {
      this.value = (value - this.min) / (this.max - this.min);
   }

   public void setValueInverted(float value) {
      this.setValue(invert(this.max, value));
   }

   public boolean isDragging() {
      return this.dragging;
   }
}
