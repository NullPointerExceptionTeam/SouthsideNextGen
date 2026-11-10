package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.client.gui.screen.Screen;

public class SetScreenEvent extends CancellableEvent {
   public final Screen screen;

   @Generated
   public SetScreenEvent(Screen screen) {
      this.screen = screen;
   }
}
