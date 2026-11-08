package dev.southside.utils.misc;

import lombok.Generated;

public final class MSTimer {
   private long time = -1L;
   private long lastMS;

   public boolean hasPassed(long timer) {
      return System.currentTimeMillis() - this.time > timer;
   }

   public boolean hasTimePassed(long MS) {
      return System.currentTimeMillis() >= this.time + MS;
   }

   public long hasTimeLeft(long MS) {
      return MS + this.time - System.currentTimeMillis();
   }

   public void resetTwo() {
      this.lastMS = this.getCurrentMS();
   }

   public long getMS() {
      return System.currentTimeMillis() - this.time;
   }

   private long getCurrentMS() {
      return System.nanoTime() / 1000000L;
   }

   public boolean hasReached(double milliseconds) {
      return this.getCurrentMS() - this.lastMS >= milliseconds;
   }

   public void reset() {
      this.time = System.currentTimeMillis();
   }

   @Generated
   public void setTime(long time) {
      this.time = time;
   }
}
