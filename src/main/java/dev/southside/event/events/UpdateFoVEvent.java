package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;

public class UpdateFoVEvent extends CancellableEvent {
   private float fov;

   public UpdateFoVEvent(float fov) {
      this.fov = fov;
   }

   @Generated
   public void setFov(float fov) {
      this.fov = fov;
   }

   @Generated
   public float getFov() {
      return this.fov;
   }
}
