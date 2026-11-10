package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

public class InteractItemEvent extends CancellableEvent {
   private Hand interactHand;
   private ItemStack item;

   @Generated
   public InteractItemEvent(Hand interactHand, ItemStack item) {
      this.interactHand = interactHand;
      this.item = item;
   }

   @Generated
   public Hand getInteractHand() {
      return this.interactHand;
   }

   @Generated
   public ItemStack getItem() {
      return this.item;
   }
}
