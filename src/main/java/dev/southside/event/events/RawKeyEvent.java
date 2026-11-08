package dev.southside.event.events;

import lombok.Generated;

public class RawKeyEvent {
   private final int key;
   private final int scancode;
   private final int action;
   private final int modifiers;

   @Generated
   public RawKeyEvent(int key, int scancode, int action, int modifiers) {
      this.key = key;
      this.scancode = scancode;
      this.action = action;
      this.modifiers = modifiers;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public int getScancode() {
      return this.scancode;
   }

   @Generated
   public int getAction() {
      return this.action;
   }

   @Generated
   public int getModifiers() {
      return this.modifiers;
   }
}
