package dev.southside.utils.render.jello.animation;

import lombok.Generated;

public class Animation {
   private Easing easing;
   private long duration;
   private long millis;
   private long startTime;
   private double startValue;
   private double destinationValue = Double.NaN;
   private double value;
   private boolean finished;

   public Animation(Easing easing, long duration) {
      this.easing = easing;
      this.startTime = System.currentTimeMillis();
      this.duration = duration;
   }

   public double animate(double destinationValue) {
      this.millis = System.currentTimeMillis();
      if (this.destinationValue != destinationValue) {
         if (!Double.isNaN(this.destinationValue)) {
            this.reset();
         }

         this.destinationValue = destinationValue;
      } else {
         this.finished = this.millis - this.duration > this.startTime;
         if (this.finished) {
            this.value = destinationValue;
            return this.value;
         }
      }

      double result = this.easing.getFunction().apply(this.getProgress());
      if (this.value > destinationValue) {
         this.value = this.startValue - (this.startValue - destinationValue) * result;
      } else {
         this.value = this.startValue + (destinationValue - this.startValue) * result;
      }

      return this.value;
   }

   public double getProgress() {
      return (double)(System.currentTimeMillis() - this.startTime) / this.duration;
   }

   public void reset() {
      this.startTime = System.currentTimeMillis();
      this.startValue = this.value;
      this.finished = false;
   }

   @Generated
   public Easing getEasing() {
      return this.easing;
   }

   @Generated
   public long getDuration() {
      return this.duration;
   }

   @Generated
   public long getMillis() {
      return this.millis;
   }

   @Generated
   public long getStartTime() {
      return this.startTime;
   }

   @Generated
   public double getStartValue() {
      return this.startValue;
   }

   @Generated
   public double getDestinationValue() {
      return this.destinationValue;
   }

   @Generated
   public double getValue() {
      return this.value;
   }

   @Generated
   public boolean isFinished() {
      return this.finished;
   }

   @Generated
   public void setEasing(Easing easing) {
      this.easing = easing;
   }

   @Generated
   public void setDuration(long duration) {
      this.duration = duration;
   }

   @Generated
   public void setMillis(long millis) {
      this.millis = millis;
   }

   @Generated
   public void setStartTime(long startTime) {
      this.startTime = startTime;
   }

   @Generated
   public void setStartValue(double startValue) {
      this.startValue = startValue;
   }

   @Generated
   public void setDestinationValue(double destinationValue) {
      this.destinationValue = destinationValue;
   }

   @Generated
   public void setValue(double value) {
      this.value = value;
   }

   @Generated
   public void setFinished(boolean finished) {
      this.finished = finished;
   }
}
