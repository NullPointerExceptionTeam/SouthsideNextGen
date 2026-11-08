package cc.polyfrost.oneconfig.internal.config.core;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import dev.southside.event.events.RawKeyEvent;
import dev.southside.event.events.RawMouseEvent;
import dev.southside.orbit.EventHandler;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class KeyBindHandler {
   public static final KeyBindHandler INSTANCE = new KeyBindHandler();
   private final ConcurrentHashMap<Entry<Field, Object>, OneKeyBind> keyBinds = new ConcurrentHashMap<>();

   @EventHandler
   private void onKeyPressed(RawKeyEvent event) {
      for (OneKeyBind keyBind : this.keyBinds.values()) {
         if (keyBind.isActive()) {
            keyBind.run();
         }
      }
   }

   @EventHandler
   private void onMousePressed(RawMouseEvent event) {
      if (event.getAction() == 1) {
         for (OneKeyBind keyBind : this.keyBinds.values()) {
            if (keyBind.isActive()) {
               keyBind.run();
            }
         }
      }
   }

   public void addKeyBind(Field field, Object instance, OneKeyBind keyBind) {
      this.keyBinds.put(new Entry<Field, Object>() {
         public Field getKey() {
            return field;
         }

         @Override
         public Object getValue() {
            return instance;
         }

         @Override
         public Object setValue(Object value) {
            return null;
         }
      }, keyBind);
   }

   public void reInitKeyBinds() {
      for (Entry<Field, Object> field : this.keyBinds.keySet()) {
         if (field.getValue() != null) {
            try {
               this.keyBinds.put(field, (OneKeyBind)field.getKey().get(field.getValue()));
            } catch (IllegalAccessException var4) {
            }
         }
      }
   }

   public void clearKeyBinds() {
      this.keyBinds.clear();
   }
}
