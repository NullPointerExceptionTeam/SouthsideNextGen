package dev.southside.event.events;

import lombok.Generated;

public class KeyPressEvent {
   private final int key;

   @Generated
   public KeyPressEvent(int key) {
      this.key = key;
   }

   @Generated
   public int getKey() {
      return this.key;
   }
}
