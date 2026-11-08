package dev.southside.event.events;

import dev.southside.event.Stage;
import lombok.Generated;

public class TickEvent {
   public Stage state;

   @Generated
   public Stage getState() {
      return this.state;
   }

   @Generated
   public void setState(Stage state) {
      this.state = state;
   }

   @Generated
   public TickEvent(Stage state) {
      this.state = state;
   }
}
