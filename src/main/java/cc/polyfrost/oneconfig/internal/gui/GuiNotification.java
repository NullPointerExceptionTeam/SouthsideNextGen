package cc.polyfrost.oneconfig.internal.gui;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.internal.utils.DescriptionRenderer;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.utils.InputHandler;

public class GuiNotification {
   private final String message;
   private final SVG icon;
   private Animation progress;
   private Animation colorAnimation = new DummyAnimation(0.0F);

   public GuiNotification(String message, float duration, SVG icon) {
      this.message = message;
      this.icon = icon;
      this.progress = new DummyAnimation(150.0F, duration);
   }

   public int draw(long vg, int x, int y, InputHandler inputHandler) {
      if (this.colorAnimation.isFinished() && !this.progress.isFinished()) {
         this.progress.get();
      }

      return DescriptionRenderer.drawDescription(
         vg,
         x,
         y,
         this.message,
         null,
         () -> this.colorAnimation,
         a -> this.colorAnimation = a,
         this.icon,
         !this.progress.isFinished(),
         DescriptionRenderer.DescriptionPosition.MIDDLE,
         inputHandler
      );
   }
}
