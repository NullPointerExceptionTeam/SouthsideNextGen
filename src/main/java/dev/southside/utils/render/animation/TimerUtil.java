package dev.southside.utils.render.animation;

public class TimerUtil {
   public long lastMS;

   public long getCurrentMS() {
      return System.nanoTime() / 1000000L;
   }

   public boolean hasReached(double milliseconds) {
      return milliseconds == 0.0 ? true : this.getCurrentMS() - this.lastMS >= milliseconds;
   }

   public boolean hasTimeElapsed(long time) {
      return System.currentTimeMillis() - this.lastMS > time;
   }

   public void reset() {
      this.lastMS = this.getCurrentMS();
   }

   public long passed() {
      return this.getCurrentMS() - this.lastMS;
   }

   public long getTime() {
      return System.currentTimeMillis() - this.lastMS;
   }

   public void setTime(long time) {
      this.lastMS = time;
   }

   public TimerUtil delay(int delay) {
      this.lastMS = this.getCurrentMS() + delay;
      return this;
   }
}
