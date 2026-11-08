package cc.polyfrost.oneconfig.gui.elements.text;

import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;

public class NumberInputField extends TextInputField {
   private final BasicElement upArrow = new BasicElement(12, 14, false);
   private final BasicElement downArrow = new BasicElement(12, 14, false);
   private final ColorAnimation colorTop = new ColorAnimation(ColorPalette.SECONDARY);
   private final ColorAnimation colorBottom = new ColorAnimation(ColorPalette.SECONDARY);
   private float min;
   private float max;
   private float step;
   private float current;

   public NumberInputField(int width, int height, float defaultValue, float min, float max, float step) {
      super(width - 16, height, true, "");
      super.onlyNums = true;
      this.min = min;
      this.max = max;
      this.step = step;
      this.setCurrentValue(defaultValue);
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      super.errored = false;
      if (this.disabled) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      nanoVGHelper.drawRoundedRect(vg, x + this.width + 4.0F, y, 12.0F, 28.0F, Colors.GRAY_500, 6.0F);
      this.upArrow.disable(this.disabled);
      this.downArrow.disable(this.disabled);
      this.upArrow.update(x + this.width + 4.0F, y, inputHandler);
      this.downArrow.update(x + this.width + 4.0F, y + 14.0F, inputHandler);

      try {
         this.current = Float.parseFloat(this.input);
      } catch (NumberFormatException var9) {
         super.errored = true;
      }

      if (!(this.current < this.min) && !(this.current > this.max)) {
         this.upArrow.disable(false);
         this.downArrow.disable(false);
      } else {
         super.errored = true;
      }

      if (this.upArrow.isClicked()) {
         this.current = this.current + this.step;
         if (this.current > this.max) {
            this.current = this.max;
         }

         this.setCurrentValue(this.current);
      }

      if (this.downArrow.isClicked()) {
         this.current = this.current - this.step;
         if (this.current < this.min) {
            this.current = this.min;
         }

         this.setCurrentValue(this.current);
      }

      if (this.current >= this.max && !this.disabled) {
         nanoVGHelper.setAlpha(vg, 0.3F);
         this.upArrow.disable(true);
      }

      nanoVGHelper.drawRoundedRectVaried(
         vg, x + this.width + 4.0F, y, 12.0F, 14.0F, this.colorTop.getColor(this.upArrow.isHovered(), this.upArrow.isPressed()), 6.0F, 6.0F, 0.0F, 0.0F
      );
      nanoVGHelper.drawSvg(vg, SVGs.CHEVRON_UP, x + this.width + 5.0F, y + 2.0F, 10.0F, 10.0F);
      if (this.current >= this.max && !this.disabled) {
         nanoVGHelper.setAlpha(vg, 1.0F);
      }

      if (this.current <= this.min && !this.disabled) {
         nanoVGHelper.setAlpha(vg, 0.3F);
         this.downArrow.disable(true);
      }

      nanoVGHelper.drawRoundedRectVaried(
         vg,
         x + this.width + 4.0F,
         y + 14.0F,
         12.0F,
         14.0F,
         this.colorBottom.getColor(this.downArrow.isHovered(), this.downArrow.isPressed()),
         0.0F,
         0.0F,
         6.0F,
         6.0F
      );
      nanoVGHelper.drawSvg(vg, SVGs.CHEVRON_DOWN, x + this.width + 5.0F, y + 15.0F, 10.0F, 10.0F);
      if (!this.disabled) {
         nanoVGHelper.setAlpha(vg, 1.0F);
      }

      try {
         super.draw(vg, x, y - 2.0F, inputHandler);
      } catch (Exception var8) {
         this.setCurrentValue(this.current);
         super.caretPos = 0;
         super.prevCaret = 0;
      }

      if (this.disabled) {
         nanoVGHelper.setAlpha(vg, 1.0F);
      }
   }

   public float getCurrentValue() {
      return this.current;
   }

   public void setCurrentValue(float value) {
      if (value == (int)value) {
         this.input = String.format("%d", (int)value);
      } else if (value * 10.0F == (int)(value * 10.0F)) {
         this.input = String.format("%.1f", value);
      } else {
         this.input = String.format("%.2f", value);
      }
   }

   @Override
   public void onClose() {
      try {
         if (this.current < this.min) {
            this.current = this.min;
         }

         if (this.current > this.max) {
            this.current = this.max;
         }

         this.setCurrentValue(this.current);
      } catch (Exception var2) {
      }
   }

   public void setStep(float step) {
      this.step = step;
   }

   public void setMax(float max) {
      this.max = max;
   }

   public void setMin(float min) {
      this.min = min;
   }

   public boolean arrowsClicked() {
      return this.upArrow.isClicked() || this.downArrow.isClicked();
   }
}
