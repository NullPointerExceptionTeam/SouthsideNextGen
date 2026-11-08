package dev.southside.utils.render.jello;

import dev.southside.MinecraftAccessor;

public class PartialTicksAnim implements MinecraftAccessor {
   private float value;
   private float lastValue;
   public boolean done;
   private static Timer timer = null;

   public PartialTicksAnim(float y) {
      this.value = y;
   }

   public static float calculateCompensation(float target, float current, double speed) {
      float diff = current - target;
      double max = speed * 3.0;
      if (diff > speed) {
         current -= (float)(max - 0.005);
         if (current < target) {
            current = target;
         }
      } else if (diff < -speed) {
         current += (float)(max + 0.005);
         if (current > target) {
            current = target;
         }
      } else {
         current = target;
      }

      return current;
   }

   public void interpolate(float targetY, double speed) {
      if (speed != 0.0) {
         this.lastValue = this.value;
         double deltaY = Math.max(Math.abs(targetY - this.value) * 0.35F / (10.0 / speed), 0.05);
         if (speed <= 0.0) {
            deltaY = -speed;
         }

         this.value = calculateCompensation(targetY, this.value, deltaY);
         if (Math.abs(targetY - this.value) <= 0.05) {
            this.value = targetY;
         }
      }
   }

   public float getValueNoTrans() {
      return this.value;
   }

   public float getValue() {
      return smoothTrans(this.value, this.lastValue);
   }

   public static float smoothTrans(double current, double last) {
      if (timer == null) {
         timer = new Timer(20.0F, System.currentTimeMillis());
      }

      float renderPartialTicks = timer.getPartialTicks(System.currentTimeMillis());
      return (float)(current * renderPartialTicks + last * (1.0F - renderPartialTicks));
   }

   public void setLValue(float y) {
      this.lastValue = y;
   }

   public void setValue(float y) {
      this.lastValue = y;
      this.value = y;
   }
}
