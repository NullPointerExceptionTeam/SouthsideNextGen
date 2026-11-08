package dev.southside.event.events;

import lombok.Generated;

public class RotationEvent {
   private boolean disableRotation;

   @Generated
   public boolean isDisableRotation() {
      return this.disableRotation;
   }

   @Generated
   public void setDisableRotation(boolean disableRotation) {
      this.disableRotation = disableRotation;
   }

   @Generated
   public RotationEvent(boolean disableRotation) {
      this.disableRotation = disableRotation;
   }
}
