package dev.southside.event.events;

import lombok.Generated;

public class StrafeEvent {
   private float yaw;

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public StrafeEvent(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
