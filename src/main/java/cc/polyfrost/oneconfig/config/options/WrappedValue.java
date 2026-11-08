package cc.polyfrost.oneconfig.config.options;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.JsonObject;

public class WrappedValue<V> {
   private final String label;
   private final Class<?> clz;
   private V value;
   private V defaultValue;

   public WrappedValue(String label, V value) {
      this.clz = value.getClass();
      this.label = label;
      this.defaultValue = value;
      this.setValue(value);
      this.reset();
   }

   public void setDefaultValue(V defaultValue) {
      this.defaultValue = defaultValue;
   }

   public Class<?> getTargetClass() {
      return this.clz;
   }

   public V getValue() {
      return this.value;
   }

   public void setValue(V value) {
      this.value = value;
   }

   public String getLabel() {
      return this.label;
   }

   public V getDefaultValue() {
      return this.defaultValue;
   }

   public void reset() {
      try {
         Config.parseConfig(Config.saveConfig(new JsonObject(), this), this);
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }
}
