package cc.polyfrost.oneconfig.gui.elements;

import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import org.jetbrains.annotations.NotNull;

public class BasicElement {
   protected int width;
   protected int height;
   protected ColorPalette colorPalette;
   protected int hitBoxX;
   protected int hitBoxY;
   protected boolean hoverFx;
   protected boolean hovered = false;
   protected boolean pressed = false;
   protected boolean clicked = false;
   protected boolean toggled = false;
   protected boolean disabled = false;
   public int currentColor;
   protected final float radius;
   private boolean block = false;
   protected ColorAnimation colorAnimation;

   public BasicElement(int width, int height, @NotNull ColorPalette colorPalette, boolean hoverFx) {
      this(width, height, colorPalette, hoverFx, 12.0F);
   }

   public BasicElement(int width, int height, @NotNull ColorPalette colorPalette, boolean hoverFx, float radius) {
      this.height = height;
      this.width = width;
      this.colorPalette = colorPalette;
      this.hoverFx = hoverFx;
      this.radius = radius;
      this.colorAnimation = new ColorAnimation(colorPalette);
   }

   public BasicElement(int width, int height, boolean hoverFx) {
      this(width, height, ColorPalette.TRANSPARENT, hoverFx, 12.0F);
   }

   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      this.update(x, y, inputHandler);
      NanoVGHelper.INSTANCE.drawRoundedRect(vg, x, y, this.width, this.height, this.currentColor, this.radius);
   }

   public void update(float x, float y, InputHandler inputHandler) {
      if (this.disabled) {
         this.hovered = false;
         this.pressed = false;
         this.clicked = false;
      } else {
         this.hovered = inputHandler.isAreaHovered(x - this.hitBoxX, y - this.hitBoxY, this.width + this.hitBoxX, this.height + this.hitBoxY);
         this.pressed = this.hovered && Platform.getMousePlatform().isButtonDown(0);
         this.clicked = inputHandler.isClicked(this.block) && this.hovered;
         if (this.clicked) {
            this.toggled = !this.toggled;
            this.onClick();
         }
      }

      if (this.hoverFx) {
         this.currentColor = this.colorAnimation.getColor(this.hovered, this.pressed);
      } else {
         this.currentColor = this.colorAnimation.getColor(false, false);
      }
   }

   public void ignoreBlockedTouches(boolean state) {
      this.block = state;
   }

   public void onClick() {
   }

   public void setCustomHitbox(int x, int y) {
      this.hitBoxX = x;
      this.hitBoxY = y;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public void setColorPalette(ColorPalette colorPalette) {
      if (this.colorPalette.equals(ColorPalette.TERTIARY) || this.colorPalette.equals(ColorPalette.TERTIARY_DESTRUCTIVE)) {
         this.colorAnimation.setColors(colorPalette.getNormalColorf());
      }

      this.colorPalette = colorPalette;
      this.colorAnimation.setPalette(colorPalette);
   }

   public ColorPalette getColorPalette() {
      return this.colorPalette;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isHovered() {
      return this.hovered;
   }

   public boolean isPressed() {
      return this.pressed;
   }

   public boolean isClicked() {
      return this.clicked;
   }

   public boolean isToggled() {
      return this.toggled;
   }

   public void setToggled(boolean toggled) {
      this.toggled = toggled;
   }

   public boolean isDisabled() {
      return this.disabled;
   }

   public void disable(boolean state) {
      this.disabled = state;
   }
}
