package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import dev.southside.event.Stage;
import net.minecraft.class_1297;

public class AttackEvent extends CancellableEvent {
   public class_1297 target;
   public final Stage stage;

   public AttackEvent(class_1297 target, Stage stage) {
      this.target = target;
      this.stage = stage;
   }
}
