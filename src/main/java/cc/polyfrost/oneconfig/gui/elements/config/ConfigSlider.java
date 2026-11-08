package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutCubic;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuart;
import cc.polyfrost.oneconfig.gui.animations.EaseOutExpo;
import cc.polyfrost.oneconfig.gui.elements.IFocusable;
import cc.polyfrost.oneconfig.gui.elements.text.NumberInputField;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.MathUtils;
import com.google.gson.internal.LazilyParsedNumber;
import java.lang.reflect.Field;

public class ConfigSlider extends BasicOption implements IFocusable {
   private static final int STEP_POPUP_DURATION = 400;
   private static final int INDICATOR_POPUP_DURATION = 200;
   private static final float STEP_HEIGHT_TOTAL = 16.0F;
   private static final float STEP_HEIGHT_HOVER = 10.0F;
   private static final float STEP_HEIGHT_DRAG = 16.0F;
   private static final float TOUCH_TARGET_TOTAL = 16.0F;
   private static final float TOUCH_TARGET_HOVER = 16.0F;
   private static final float TOUCH_TARGET_DRAG = 10.0F;
   private final NumberInputField inputField;
   private final float min;
   private final float max;
   private final int step;
   private final boolean instant;
   private final float increment;
   private boolean isFloat = true;
   private boolean dragging = false;
   private boolean mouseWasDown = false;
   private Animation stepsAnimation;
   private Animation targetAnimation;
   private Animation stepSlideAnimation;
   private float animationStart;
   private float lastSliderTarget = 1.0F;
   private boolean animReset;
   private float lastX = -1.0F;

   public ConfigSlider(Field field, Object parent, String name, String description, String category, String subcategory, float min, float max, int step) {
      this(field, parent, name, description, category, subcategory, min, max, step, false, 0.1F);
   }

   public ConfigSlider(
      Field field,
      Object parent,
      String name,
      String description,
      String category,
      String subcategory,
      float min,
      float max,
      int step,
      boolean instant,
      float increment
   ) {
      super(field, parent, name, description, category, subcategory, 2);
      this.min = min;
      this.max = max;
      this.step = step;
      this.instant = instant;
      this.increment = increment;
      this.inputField = new NumberInputField(84, 32, 0.0F, min, max, step == 0 ? 1.0F : step);
      this.stepsAnimation = new DummyAnimation(0.0F);
      this.targetAnimation = new DummyAnimation(0.0F);
      this.stepSlideAnimation = new DummyAnimation(1.0F);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      int xCoordinate = 0;
      float value = 0.0F;
      boolean hovered = inputHandler.isAreaHovered(x + 352, y, 512.0F, 32.0F) && this.isEnabled();
      this.inputField.disable(!this.isEnabled());
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      boolean isMouseDown = Platform.getMousePlatform().isButtonDown(0);
      if (hovered && isMouseDown && !this.mouseWasDown) {
         this.dragging = true;
      }

      boolean startedDragging = !this.mouseWasDown && isMouseDown;
      this.mouseWasDown = isMouseDown;
      if (this.dragging) {
         xCoordinate = (int)MathUtils.clamp(inputHandler.mouseX(), x + 352, x + 864);
         if (this.step > 0) {
            xCoordinate = this.getStepCoordinate(xCoordinate, x);
         }

         value = (float)MathUtils.mapIncrement(xCoordinate, x + 352, x + 864, this.min, this.max, this.increment);
      } else if (this.inputField.isToggled() || this.inputField.arrowsClicked()) {
         value = this.inputField.getCurrentValue();
         xCoordinate = (int)MathUtils.clamp(MathUtils.map(value, this.min, this.max, x + 352, x + 864), x + 352, x + 864);
      }

      if ((!this.dragging || !inputHandler.isClicked()) && !this.inputField.isToggled() && !this.inputField.arrowsClicked()) {
         if (this.dragging && this.instant) {
            this.setValue(value);
         }
      } else {
         this.dragging = false;
         if (this.step > 0) {
            xCoordinate = this.getStepCoordinate(xCoordinate, x);
            value = (float)MathUtils.mapIncrement(xCoordinate, x + 352, x + 864, this.min, this.max, this.increment);
         }

         this.setValue(value);
      }

      float stepPercent = this.stepsAnimation.get();
      float targetPercent = this.targetAnimation.get();
      if (this.isEnabled()) {
         if (this.dragging && startedDragging) {
            this.stepsAnimation = new EaseOutExpo(400, stepPercent, 1.0F, false);
            this.targetAnimation = new EaseOutExpo(200, targetPercent, 0.625F, false);
            this.animReset = true;
         } else if (!this.dragging && hovered) {
            if (this.targetAnimation.getEnd() != 1.0F) {
               this.stepsAnimation = new EaseOutExpo(400, stepPercent, 0.625F, false);
               this.targetAnimation = new EaseInOutQuart(200, targetPercent, 1.0F, false);
               this.animReset = true;
            }
         } else if (!this.dragging && this.animReset) {
            this.stepsAnimation = new EaseOutExpo(400, stepPercent, 0.0F, false);
            this.targetAnimation = new EaseOutExpo(200, targetPercent, 0.0F, false);
            this.animReset = false;
         }
      }

      if (!this.dragging && !this.inputField.isToggled()) {
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
            } catch (ClassCastException var19) {
               System.err.println("?");
            }

            xCoordinate = (int)MathUtils.clamp(MathUtils.map(value, this.min, this.max, x + 352, x + 864), x + 352, x + 864);
         } catch (IllegalAccessException var20) {
         }
      }

      if (!this.inputField.isToggled()) {
         this.inputField.setCurrentValue(value);
      }

      if (this.stepSlideAnimation.isFinished()
         && this.lastSliderTarget != -1.0F
         && this.lastSliderTarget != xCoordinate
         && (!this.dragging || startedDragging || this.step > 0)
         && this.lastX == x) {
         this.animationStart = this.lastSliderTarget;
         this.stepSlideAnimation = new EaseInOutCubic(Preferences.INSTANCE.trackerResponseDuration.getValue().intValue(), 0.0F, 1.0F, false);
      }

      float progress = this.stepSlideAnimation.get();
      this.lastSliderTarget = xCoordinate;
      this.lastX = x;
      xCoordinate = (int)(xCoordinate * progress + this.animationStart * (1.0F - progress));
      float radius = 4.0F;
      if (this.step > 0) {
         radius *= 1.0F - Math.min(stepPercent, 0.625F) * 16.0F / 10.0F;
      }

      nanoVGHelper.drawText(vg, this.name, x, y + 17, this.nameColor, 14.0F, Fonts.MEDIUM);
      nanoVGHelper.drawRoundedRect(vg, x + 352, y + 13, 512.0F, 4.0F, Colors.GRAY_300, radius);
      nanoVGHelper.drawRoundedRect(vg, x + 352, y + 13 - 1, xCoordinate - x - 352, 6.0F, Colors.PRIMARY_500, 4.0F);
      if (this.step > 0 && stepPercent > 0.05F) {
         float stepOffset = stepPercent * 16.0F;

         for (float i = x + 354; i <= x + 864; i += 512.0F / ((this.max - this.min) / this.step)) {
            int color = xCoordinate > i - 2.0F ? Colors.PRIMARY_500 : Colors.GRAY_300;
            nanoVGHelper.drawRoundedRect(vg, i - 2.0F, y + 16 - 1 - stepOffset / 2.0F, 4.0F, stepOffset, color, 2.0F);
         }
      }

      nanoVGHelper.drawRoundedRect(vg, xCoordinate - 12, y + 4, 24.0F, 24.0F, Colors.WHITE, 12.0F);
      if (targetPercent > 0.02F) {
         nanoVGHelper.drawRoundedRect(
            vg, xCoordinate - 8.0F * targetPercent, y + 16 - 8.0F * targetPercent, 16.0F * targetPercent, 16.0F * targetPercent, Colors.PRIMARY_500, 12.0F
         );
      }

      this.inputField.draw(vg, x + 892, y, inputHandler);
      nanoVGHelper.setAlpha(vg, 1.0F);
   }

   private int getStepCoordinate(int xCoordinate, int x) {
      Integer nearest = null;
      float i = x + 352;

      while (i <= x + 864) {
         if (nearest == null || Math.abs(xCoordinate - i) < Math.abs(xCoordinate - nearest)) {
            nearest = (int)i;
         }

         i += 512.0F / ((this.max - this.min) / this.step);
      }

      return nearest == null ? 0 : nearest;
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
   public boolean hasFocus() {
      return this.inputField.isToggled();
   }
}
