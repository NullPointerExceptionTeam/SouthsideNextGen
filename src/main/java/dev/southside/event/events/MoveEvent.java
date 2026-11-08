package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;

public class MoveEvent extends CancellableEvent {
   public double x;
   public double y;
   public double z;

   public MoveEvent(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   @Generated
   public double getX() {
      return this.x;
   }

   @Generated
   public double getY() {
      return this.y;
   }

   @Generated
   public double getZ() {
      return this.z;
   }

   @Generated
   public void setX(double x) {
      this.x = x;
   }

   @Generated
   public void setY(double y) {
      this.y = y;
   }

   @Generated
   public void setZ(double z) {
      this.z = z;
   }
}
