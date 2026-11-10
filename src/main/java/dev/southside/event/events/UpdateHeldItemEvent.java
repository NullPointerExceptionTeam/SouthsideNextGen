package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

public class UpdateHeldItemEvent extends CancellableEvent {
   private final Hand hand;
   private ItemStack item;

   @Generated
   public Hand getHand() {
      return this.hand;
   }

   @Generated
   public ItemStack getItem() {
      return this.item;
   }

   @Generated
   public void setItem(ItemStack item) {
      this.item = item;
   }

   @Generated
   public UpdateHeldItemEvent(Hand hand, ItemStack item) {
      this.hand = hand;
      this.item = item;
   }
}
