package cc.polyfrost.oneconfig.gui.animations;

import cc.polyfrost.oneconfig.utils.gui.GuiUtils;

public abstract class Animation {
   protected final boolean reverse;
   protected final float duration;
   protected final float start;
   protected final float change;
   protected float timePassed = 0.0F;

   public Animation(float duration, float start, float end, boolean reverse) {
      this.duration = duration;
      if (reverse) {
         float temp = start;
         start = end;
         end = temp;
      }

      this.start = start;
      this.change = end - start;
      this.reverse = reverse;
   }

   public float get(float deltaTime) {
      this.timePassed += deltaTime;
      return this.timePassed >= this.duration ? this.start + this.change : this.animate(this.timePassed / this.duration) * this.change + this.start;
   }

   public float get() {
      return this.get(GuiUtils.getDeltaTime());
   }

   public boolean isFinished() {
      return this.timePassed >= this.duration;
   }

   public boolean isReversed() {
      return this.reverse;
   }

   public float getStart() {
      return this.start;
   }

   public float getEnd() {
      return this.start + this.change;
   }

   protected abstract float animate(float var1);
}
