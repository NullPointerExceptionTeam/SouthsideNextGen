package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;

public class SprintPacketSendEvent extends CancellableEvent {
   private boolean sprinting;

   @Generated
   public SprintPacketSendEvent(boolean sprinting) {
      this.sprinting = sprinting;
   }

   @Generated
   public boolean isSprinting() {
      return this.sprinting;
   }

   @Generated
   public void setSprinting(boolean sprinting) {
      this.sprinting = sprinting;
   }
}
