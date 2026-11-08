package dev.southside.utils.render.animation;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutCubic;
import cc.polyfrost.oneconfig.gui.animations.EaseOutQuad;

public class AnimationComponent {
   private Animation xAnimation;
   private Animation yAnimation;
   private Animation alphaAnimation;
   private Animation backgroundWidthAnimation;
   private Animation backgroundHeightAnimation;
   private Animation backgroundAlphaAnimation;

   public AnimationComponent(int animationDuration, float initialX, float initialY, float initialAlpha) {
      this(animationDuration, initialX, initialY, initialAlpha, 0.0F, 0.0F);
   }

   public AnimationComponent(
      int animationDuration, float initialX, float initialY, float initialAlpha, float initialBackgroundWidth, float initialBackgroundHeight
   ) {
      this.xAnimation = new EaseInOutCubic(animationDuration, initialX, initialX, false);
      this.yAnimation = new EaseInOutCubic(animationDuration, initialY, initialY, false);
      this.alphaAnimation = new EaseInOutCubic(animationDuration, initialAlpha, initialAlpha, false);
      this.backgroundWidthAnimation = new EaseOutQuad(animationDuration, initialBackgroundWidth, initialBackgroundWidth, false);
      this.backgroundHeightAnimation = new EaseOutQuad(animationDuration, initialBackgroundHeight, initialBackgroundHeight, false);
      this.backgroundAlphaAnimation = new EaseOutQuad(animationDuration, initialAlpha, initialAlpha, false);
   }

   public AnimationComponent(
      int animationDuration,
      float initialX,
      float initialY,
      float initialAlpha,
      float initialBackgroundWidth,
      float initialBackgroundHeight,
      float endX,
      float endY,
      float endAlpha,
      float endBackgroundWidth,
      float endBackgroundHeight
   ) {
      this.xAnimation = new EaseInOutCubic(animationDuration, initialX, endX, false);
      this.yAnimation = new EaseInOutCubic(animationDuration, initialY, endY, false);
      this.alphaAnimation = new EaseInOutCubic(animationDuration, initialAlpha, endAlpha, false);
      this.backgroundWidthAnimation = new EaseOutQuad(animationDuration, initialBackgroundWidth, endBackgroundWidth, false);
      this.backgroundHeightAnimation = new EaseOutQuad(animationDuration, initialBackgroundHeight, endBackgroundHeight, false);
      this.backgroundAlphaAnimation = new EaseOutQuad(animationDuration, initialAlpha, endAlpha, false);
   }

   public void updateTarget(int animationDuration, float targetX, float targetY, float targetAlpha) {
      this.xAnimation = new EaseInOutCubic(animationDuration, this.xAnimation.get(), targetX, false);
      this.yAnimation = new EaseInOutCubic(animationDuration, this.yAnimation.get(), targetY, false);
      this.alphaAnimation = new EaseOutQuad(animationDuration, this.alphaAnimation.get(), targetAlpha, false);
   }

   public void updateBackgroundTarget(int animationDuration, float targetWidth, float targetHeight, float targetAlpha) {
      this.backgroundWidthAnimation = new EaseOutQuad(animationDuration, this.backgroundWidthAnimation.get(), targetWidth, false);
      this.backgroundHeightAnimation = new EaseOutQuad(animationDuration, this.backgroundHeightAnimation.get(), targetHeight, false);
      this.backgroundAlphaAnimation = new EaseOutQuad(animationDuration, this.backgroundAlphaAnimation.get(), targetAlpha, false);
   }

   public float getX() {
      return this.xAnimation.get();
   }

   public float getY() {
      return this.yAnimation.get();
   }

   public float getAlpha() {
      return this.alphaAnimation.get();
   }

   public float getBackgroundWidth() {
      return this.backgroundWidthAnimation.get();
   }

   public float getBackgroundHeight() {
      return this.backgroundHeightAnimation.get();
   }

   public float getBackgroundAlpha() {
      return this.backgroundAlphaAnimation.get();
   }

   public float getTargetX() {
      return this.xAnimation.getEnd();
   }

   public float getTargetY() {
      return this.yAnimation.getEnd();
   }

   public float getTargetAlpha() {
      return this.alphaAnimation.getEnd();
   }

   public boolean isFinished(boolean background) {
      return background
         ? this.backgroundWidthAnimation.isFinished() && this.backgroundHeightAnimation.isFinished() && this.backgroundAlphaAnimation.isFinished()
         : this.xAnimation.isFinished() && this.yAnimation.isFinished() && this.alphaAnimation.isFinished();
   }
}
