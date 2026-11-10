package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import dev.southside.event.Stage;
import net.minecraft.entity.Entity;

public class AttackEvent extends CancellableEvent {
   public Entity target;
   public final Stage stage;

   public AttackEvent(Entity target, Stage stage) {
      this.target = target;
      this.stage = stage;
   }
}
