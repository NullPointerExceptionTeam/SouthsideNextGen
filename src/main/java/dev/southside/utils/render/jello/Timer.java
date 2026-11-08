package dev.southside.utils.render.jello;

public class Timer {
   public float renderPartialTicks;
   public float elapsedPartialTicks;
   private long lastSyncSysClock;
   public float tickLength;

   public Timer(float ticks, long lastSyncSysClock) {
      this.tickLength = 1000.0F / ticks;
      this.lastSyncSysClock = lastSyncSysClock;
   }

   public float getTimerSpeed() {
      return this.tickLength / 50.0F;
   }

   public void setTimerSpeed(float speed) {
      this.tickLength = 50.0F / speed;
   }

   public int getPartialTicks(long gameTime) {
      this.elapsedPartialTicks = (float)(gameTime - this.lastSyncSysClock) / this.tickLength;
      this.lastSyncSysClock = gameTime;
      this.renderPartialTicks = this.renderPartialTicks + this.elapsedPartialTicks;
      int i = (int)this.renderPartialTicks;
      this.renderPartialTicks -= i;
      return i;
   }
}
