package dev.southside.event;

import dev.southside.orbit.ICancellable;

public abstract class CancellableEvent implements ICancellable {
   private boolean cancelled;

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
