package dev.southside.orbit;

import dev.southside.orbit.listeners.IListener;
import dev.southside.orbit.listeners.LambdaListener;

public interface IEventBus {
   void registerLambdaFactory(String var1, LambdaListener.Factory var2);

   boolean isListening(Class<?> var1);

   <T> T post(T var1);

   default <T> T call(T event) {
      return this.post(event);
   }

   <T extends ICancellable> T post(T var1);

   default <T extends ICancellable> T call(T event) {
      return this.post(event);
   }

   void subscribe(Object var1);

   default void register(Object object) {
      this.subscribe(object);
   }

   void subscribe(Class<?> var1);

   default void register(Class<?> klass) {
      this.subscribe(klass);
   }

   void subscribe(IListener var1);

   default void register(IListener listener) {
      this.subscribe(listener);
   }

   void unsubscribe(Object var1);

   default void unregister(Object object) {
      this.unsubscribe(object);
   }

   void unsubscribe(Class<?> var1);

   default void unregister(Class<?> klass) {
      this.unsubscribe(klass);
   }

   void unsubscribe(IListener var1);

   default void unregister(IListener listener) {
      this.unsubscribe(listener);
   }
}
