package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;

public class ChatEvent extends CancellableEvent {
   public String message;

   @Generated
   public String getMessage() {
      return this.message;
   }

   @Generated
   public ChatEvent(String message) {
      this.message = message;
   }

   @Generated
   public void setMessage(String message) {
      this.message = message;
   }
}
