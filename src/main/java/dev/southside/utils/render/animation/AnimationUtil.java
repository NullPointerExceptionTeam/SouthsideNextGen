package dev.southside.utils.render.animation;

import dev.southside.MinecraftAccessor;

public class AnimationUtil {
   private final TimerUtil timer;
   private float speed;
   private float prevValue;
   private float scale;
   public float target;
   private boolean frozen;
   private float frozenValue;

   public AnimationUtil(float speed, float value) {
      this.speed = speed;
      this.timer = new TimerUtil();
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
         long delta = this.timer.passed();
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

   public static double animate(double current, double endPoint, double speed) {
      if (speed >= 1.0) {
         return endPoint;
      } else {
         return speed == 0.0 ? current : thunder(current, endPoint, speed);
      }
   }

   public static double mio(double current, double endPoint, double speed) {
      if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
         return endPoint;
      } else {
         int negative = speed < 0.0 ? -1 : 1;
         if (negative == -1) {
            speed *= -1.0;
         }

         double diff = endPoint - current;
         double factor = diff
            * MinecraftAccessor.mc.method_61966().method_60637(false)
            / (1.0 / speed * (Math.min(240.0, (double)MinecraftAccessor.mc.method_47599()) / 240.0));
         if (diff < 0.0 && factor < diff) {
            factor = diff;
         } else if (diff > 0.0 && factor >= diff) {
            factor = diff;
         }

         return current + factor * negative;
      }
   }

   public static double old(double current, double endPoint, double speed) {
      if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
         return endPoint;
      } else {
         int negative = speed < 0.0 ? -1 : 1;
         if (negative == -1) {
            speed *= -1.0;
         }

         double diff = endPoint - current;
         double factor = diff * speed;
         if (diff < 0.0 && factor < diff) {
            factor = diff;
         } else if (diff > 0.0 && factor >= diff) {
            factor = diff;
         }

         return current + factor * negative;
      }
   }

   public static double my(double current, double endPoint, double speed) {
      if (Math.max(endPoint, current) - Math.min(endPoint, current) < 0.001) {
         return endPoint;
      } else {
         int negative = speed < 0.0 ? -1 : 1;
         if (negative == -1) {
            speed *= -1.0;
         }

         double diff = endPoint - current;
         double factor = diff * MinecraftAccessor.mc.method_61966().method_60637(false) * speed;
         if (diff < 0.0 && factor < diff) {
            factor = diff;
         } else if (diff > 0.0 && factor >= diff) {
            factor = diff;
         }

         return current + factor * negative;
      }
   }

   public static double thunder(double current, double endPoint, double speed) {
      boolean shouldContinueAnimation = endPoint > current;
      double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
      if (Math.abs(dif) <= 0.001) {
         return endPoint;
      } else {
         double factor = dif * speed;
         return current + (shouldContinueAnimation ? factor : -factor);
      }
   }

   public static double normal(double current, double endPoint, double speed) {
      boolean shouldContinueAnimation = endPoint > current;
      speed *= 10.0;
      return Math.abs(Math.max(endPoint, current) - Math.min(endPoint, current)) <= speed ? endPoint : current + (shouldContinueAnimation ? speed : -speed);
   }
}
