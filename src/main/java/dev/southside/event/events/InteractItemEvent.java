package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.class_1268;
import net.minecraft.class_1799;

public class InteractItemEvent extends CancellableEvent {
   private class_1268 interactHand;
   private class_1799 item;

   @Generated
   public InteractItemEvent(class_1268 interactHand, class_1799 item) {
      this.interactHand = interactHand;
      this.item = item;
   }

   @Generated
   public class_1268 getInteractHand() {
      return this.interactHand;
   }

   @Generated
   public class_1799 getItem() {
      return this.item;
   }
}
