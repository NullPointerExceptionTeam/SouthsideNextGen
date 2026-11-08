package cc.polyfrost.oneconfig.events;

import cc.polyfrost.oneconfig.events.event.PreShutdownEvent;
import cc.polyfrost.oneconfig.events.event.ShutdownEvent;
import dev.southside.Southside;
import dev.southside.orbit.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class EventManager {
   public static final EventManager INSTANCE = new EventManager();
   private static final Logger LOGGER = LogManager.getLogger("OneConfig/EventManager");
   private final IEventBus eventBus = Southside.get().getEventBus();

   public EventManager() {
      this.eventBus.subscribe(this);
   }

   public void preStop() {
      this.post(new PreShutdownEvent());
   }

   public void postStop() {
      this.post(new ShutdownEvent());
   }

   public IEventBus getEventBus() {
      return this.eventBus;
   }

   public void register(Object object) {
      this.eventBus.register(object);
   }

   public void unregister(Object object) {
      this.eventBus.unregister(object);
   }

   public void post(Object event) {
      this.eventBus.post(event);
   }
}
