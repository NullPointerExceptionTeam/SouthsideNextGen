package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.class_1268;
import net.minecraft.class_1799;

public class UpdateHeldItemEvent extends CancellableEvent {
   private final class_1268 hand;
   private class_1799 item;

   @Generated
   public class_1268 getHand() {
      return this.hand;
   }

   @Generated
   public class_1799 getItem() {
      return this.item;
   }

   @Generated
   public void setItem(class_1799 item) {
      this.item = item;
   }

   @Generated
   public UpdateHeldItemEvent(class_1268 hand, class_1799 item) {
      this.hand = hand;
      this.item = item;
   }
}
