package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.class_437;

public class SetScreenEvent extends CancellableEvent {
   public final class_437 screen;

   @Generated
   public SetScreenEvent(class_437 screen) {
      this.screen = screen;
   }
}
