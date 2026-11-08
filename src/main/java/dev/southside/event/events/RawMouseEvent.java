package dev.southside.event.events;

import lombok.Generated;

public class RawMouseEvent {
   private final int button;
   private final int action;
   private final int mods;

   @Generated
   public RawMouseEvent(int button, int action, int mods) {
      this.button = button;
      this.action = action;
      this.mods = mods;
   }

   @Generated
   public int getButton() {
      return this.button;
   }

   @Generated
   public int getAction() {
      return this.action;
   }

   @Generated
   public int getMods() {
      return this.mods;
   }
}
