package cc.polyfrost.oneconfig.platform.impl;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.libs.universal.UMouse;
import cc.polyfrost.oneconfig.platform.MousePlatform;
import dev.southside.mixin.accessors.IMouse;
import dev.southside.mixin.accessors.IScroller;
import org.lwjgl.glfw.GLFW;

public class MousePlatformImpl implements MousePlatform {
   private double prevScroll = 0.0;

   @Override
   public double getMouseX() {
      return UMouse.Raw.getX();
   }

   @Override
   public double getMouseY() {
      return UMouse.Raw.getY();
   }

   @Override
   public double getDWheel() {
      double scrollDelta = ((IScroller)((IMouse)UMinecraft.getMinecraft().field_1729).getScroller()).getCumulVertical();
      double amount = scrollDelta - this.prevScroll;
      this.prevScroll = scrollDelta;
      return amount;
   }

   @Override
   public double getMouseDX() {
      return ((IMouse)UMinecraft.getMinecraft().field_1729).getCursorDeltaX();
   }

   @Override
   public double getMouseDY() {
      return ((IMouse)UMinecraft.getMinecraft().field_1729).getCursorDeltaY();
   }

   @Override
   public int getButtonState(int button) {
      return GLFW.glfwGetMouseButton(UMinecraft.getWindowHandle(), button);
   }

   @Override
   public boolean isButtonDown(int button) {
      return GLFW.glfwGetMouseButton(UMinecraft.getWindowHandle(), button) == 1;
   }
}
