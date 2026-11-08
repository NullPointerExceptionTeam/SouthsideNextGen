package cc.polyfrost.oneconfig.gui.animations;

import java.util.concurrent.Callable;

public class DummyAnimation extends Animation {
   protected final float value;
   protected Callable<Boolean> done = null;

   public DummyAnimation(float value, Callable<Boolean> done) {
      super(0.0F, value, value, false);
      this.value = value;
      this.done = done;
   }

   public DummyAnimation(float value, float duration) {
      super(duration, value, value, false);
      this.value = value;
   }

   public DummyAnimation(float value) {
      this(value, 0.0F);
   }

   @Override
   public float get(float deltaTime) {
      this.timePassed += deltaTime;
      return this.value;
   }

   @Override
   public boolean isFinished() {
      if (this.done != null) {
         try {
            return this.done.call();
         } catch (Exception var2) {
         }
      }

      return super.isFinished();
   }

   @Override
   protected float animate(float x) {
      return x;
   }
}
