package dev.southside.event.events;

import lombok.Generated;

public class KeyboardInputEvent {
   private final int key;

   @Generated
   public KeyboardInputEvent(int key) {
      this.key = key;
   }

   @Generated
   public int getKey() {
      return this.key;
   }
}
