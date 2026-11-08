package dev.southside.gui.netease;

import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.util.concurrent.Future;
import org.jetbrains.annotations.NotNull;

public class LoadingDialog extends BasicElement {
   public float x;
   public float y;
   public String text;
   public boolean showing;
   public Future<?> task;
   private float scale = 0.0F;
   private final float speed = 0.15F;
   private float rotation = 0.0F;

   public LoadingDialog(int width, int height, @NotNull ColorPalette colorPalette) {
      super(width, height, colorPalette, true);
   }

   public void setTask(Future<?> t, String label) {
      this.text = label;
      this.task = t;
      this.showing = !t.isDone();
   }

   private float easeInOut(float t) {
      return t < 0.5F ? 2.0F * t * t : -1.0F + (4.0F - 2.0F * t) * t;
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.x = x;
      this.y = y;
      this.update(x, y, inputHandler);
      if (this.task == null || this.task.isDone()) {
         this.showing = false;
      }

      if (this.showing || !(this.scale <= 0.01F)) {
         float target = this.showing ? 1.0F : 0.0F;
         this.scale = this.scale + (target - this.scale) * 0.15F;
         float easedScale = this.easeInOut(this.scale);
         float centerX = x + this.width / 2.0F;
         float centerY = y + this.height / 2.0F;
         float lastAlpha = nanoVGHelper.lastAlpha();
         nanoVGHelper.setAlpha(vg, easedScale);
         nanoVGHelper.save(vg);
         nanoVGHelper.translate(vg, centerX, centerY);
         nanoVGHelper.scale(vg, easedScale, easedScale);
         nanoVGHelper.translate(vg, -centerX, -centerY);
         nanoVGHelper.drawRoundedRect(vg, x, y, this.width, this.height, this.currentColor, this.height / 2.0F);
         float radius = Math.min(this.width, this.height) / 4.0F;
         if (this.showing && easedScale > 0.01F) {
            this.rotation += 4.0F;
            float cx = x + radius + 15.0F;
            nanoVGHelper.save(vg);
            nanoVGHelper.translate(vg, cx, centerY);
            nanoVGHelper.rotate(vg, this.rotation);
            nanoVGHelper.drawArc(vg, 0.0F, 0.0F, radius, 0.0F, (float)Math.toRadians(270.0), 2, -1, 3.0F);
            nanoVGHelper.restore(vg);
         }

         if (this.text != null) {
            nanoVGHelper.drawCenteredText(vg, this.text, centerX, centerY, Colors.WHITE, 18.0F, Fonts.REGULAR);
         }

         nanoVGHelper.restore(vg);
         nanoVGHelper.setAlpha(vg, lastAlpha);
      }
   }
}
