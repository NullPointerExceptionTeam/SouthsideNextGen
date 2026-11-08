package dev.southside.utils.timer;

import lombok.Generated;

public class StopWatch {
   private long millis;

   public StopWatch() {
      this.reset();
   }

   public boolean finished(long delay) {
      return System.currentTimeMillis() - delay >= this.millis;
   }

   public void reset() {
      this.millis = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      return System.currentTimeMillis() - this.millis;
   }

   @Generated
   public long getMillis() {
      return this.millis;
   }

   @Generated
   public void setMillis(long millis) {
      this.millis = millis;
   }
}
