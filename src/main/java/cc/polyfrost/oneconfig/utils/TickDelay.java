package cc.polyfrost.oneconfig.utils;

import cc.polyfrost.oneconfig.events.EventManager;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;

public class TickDelay {
   private final Runnable function;
   private int delay;

   public TickDelay(Runnable function, int ticks) {
      if (ticks < 1) {
         function.run();
      } else {
         EventManager.INSTANCE.register(this);
         this.delay = ticks;
      }

      this.function = function;
   }

   @EventHandler
   protected void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (this.delay < 1) {
            this.function.run();
            EventManager.INSTANCE.unregister(this);
         }

         this.delay--;
      }
   }
}
