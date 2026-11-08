package cc.polyfrost.oneconfig.utils;

import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import java.util.ArrayList;

public class InputHandler {
   private final ArrayList<Scissor> blockScissors = new ArrayList<>();
   private double scaleX = 1.0;
   private double scaleY = 1.0;
   private boolean blockDWheel = false;
   private double accumulatedDWheel = 0.0;

   public void scale(double scaleX, double scaleY) {
      this.scaleX = scaleX;
      this.scaleY = scaleY;
   }

   public void resetScale() {
      this.scaleX = 1.0;
      this.scaleY = 1.0;
   }

   public double getXScaleFactor() {
      return this.scaleX;
   }

   public double getYScaleFactor() {
      return this.scaleY;
   }

   public boolean isAreaHovered(float x, float y, float width, float height, boolean ignoreBlock) {
      float mouseX = this.mouseX();
      float mouseY = this.mouseY();
      return (ignoreBlock || this.blockScissors.size() == 0 || !this.shouldBlock(mouseX, mouseY))
         && mouseX > x
         && mouseY > y
         && mouseX < x + width
         && mouseY < y + height;
   }

   public boolean isAreaHovered(float x, float y, float width, float height) {
      return this.isAreaHovered(x, y, width, height, false);
   }

   public boolean isAreaClicked(float x, float y, float width, float height, boolean ignoreBlock) {
      return this.isAreaHovered(x, y, width, height, ignoreBlock) && this.isClicked(false);
   }

   public boolean isAreaClicked(float x, float y, float width, float height) {
      return this.isAreaClicked(x, y, width, height, false);
   }

   public boolean isClicked(boolean ignoreBlock) {
      return GuiUtils.wasMouseDown()
         && !Platform.getMousePlatform().isButtonDown(0)
         && (ignoreBlock || this.blockScissors.size() == 0 || !this.shouldBlock(this.mouseX(), this.mouseY()));
   }

   public boolean isClicked() {
      return this.isClicked(false);
   }

   public boolean isMouseDown(int button) {
      return Platform.getMousePlatform().isButtonDown(button);
   }

   public boolean isMouseDown() {
      return this.isMouseDown(0);
   }

   public float mouseX() {
      return (float)(Platform.getMousePlatform().getMouseX() / this.scaleX);
   }

   public float mouseY() {
      return (float)(Platform.getMousePlatform().getMouseY() / this.scaleY);
   }

   public Scissor blockInputArea(float x, float y, float width, float height) {
      Scissor scissor = new Scissor(new Scissor(x, y, width, height));
      this.blockScissors.add(scissor);
      return scissor;
   }

   public void blockInputArea(Scissor inputScissor) {
      this.blockScissors.add(inputScissor);
   }

   public Scissor blockAllInput() {
      return this.blockInputArea(0.0F, 0.0F, 1920.0F, 1080.0F);
   }

   public void stopBlock(Scissor scissor) {
      this.blockScissors.remove(scissor);
   }

   public void stopBlockingInput() {
      this.blockScissors.clear();
   }

   public boolean isBlockingInput() {
      return !this.blockScissors.isEmpty();
   }

   public double getDWheel(boolean ignoreBlock) {
      double result;
      if (this.accumulatedDWheel != 0.0) {
         result = this.accumulatedDWheel;
      } else {
         result = Platform.getMousePlatform().getDWheel();
      }

      this.accumulatedDWheel = 0.0;
      return !ignoreBlock && this.blockDWheel ? 0.0 : result;
   }

   public double getDWheel() {
      return this.getDWheel(false);
   }

   public void pushDWheel(double amount) {
      this.accumulatedDWheel += amount;
   }

   public void blockDWheel() {
      this.blockDWheel = true;
   }

   public void unblockDWheel() {
      this.blockDWheel = false;
   }

   private boolean shouldBlock(float x, float y) {
      for (Scissor block : this.blockScissors) {
         if (block.isInScissor(x, y)) {
            return true;
         }
      }

      return false;
   }
}
