package dev.southside.utils.misc;

import dev.southside.utils.timer.StopWatch;

public class BezierUtil {
   private final StopWatch timer;
   private final float speed;
   private float prevValue;
   private float scale;
   public float target;
   private boolean frozen;
   private float frozenValue;

   public BezierUtil(float speed, float value) {
      this.speed = speed;
      this.timer = new StopWatch();
      this.update(value);
      this.set(value);
   }

   public void update(float target) {
      if (this.target != target) {
         this.prevValue = this.get();
         this.timer.reset();
         this.scale = target - this.prevValue;
         this.target = target;
      }
   }

   public void set(float value) {
      this.timer.reset();
      this.prevValue = value;
      this.scale = this.target - this.prevValue;
   }

   public float get() {
      if (this.frozen) {
         return this.frozenValue;
      } else {
         long delta = this.timer.getElapsedTime();
         return this.prevValue + this.scale * ease(Math.min((float)delta / 1000.0F * this.speed, 1.0F));
      }
   }

   public void freeze() {
      this.frozen = false;
      this.frozenValue = this.get();
      this.frozen = true;
   }

   private static float ease(float t) {
      t = 1.0F - t;
      float p0 = 0.0F;
      float p1 = 0.01F;
      float p2 = 0.0F;
      float p3 = 1.0F;
      float tt = t * t;
      float ttt = tt * t;
      float q0 = -p0 * ttt + 3.0F * p0 * tt - 3.0F * p0 * t + p0;
      float q1 = 3.0F * p1 * ttt - 6.0F * p1 * tt + 3.0F * p1 * t;
      float q2 = -3.0F * p2 * ttt + 3.0F * p2 * tt;
      float q3 = p3 * ttt;
      return 1.0F - (q0 + q1 + q2 + q3);
   }
}
