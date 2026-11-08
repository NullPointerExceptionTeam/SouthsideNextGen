package dev.southside.event.events;

import dev.southside.event.Stage;
import lombok.Generated;

public class GameRendererDrawEvent {
   public final Stage stage;
   public final float tickDelta;

   @Generated
   public GameRendererDrawEvent(Stage stage, float tickDelta) {
      this.stage = stage;
      this.tickDelta = tickDelta;
   }
}
