package cc.polyfrost.oneconfig.config.options;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import com.google.gson.JsonObject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class Option<V> implements IOption {
   private final String label;
   private final V defaultValue;
   public V value;
   public transient BasicOption basicOption = null;

   public Option(@NotNull String label, @NotNull V defaultValue) {
      this.label = Objects.requireNonNull(label);
      this.defaultValue = defaultValue;
      this.value = defaultValue;
      this.reset();
   }

   public String getLabel() {
      return this.label;
   }

   public V getDefaultValue() {
      return this.defaultValue;
   }

   public void setValue(V value) {
      this.value = value;
      if (this.basicOption != null) {
         this.basicOption.triggerListeners();
      }
   }

   public V getValue() {
      return this.value;
   }

   public void reset() {
      try {
         this.value = this.defaultValue;
         Config.parseConfig(Config.saveConfig(new JsonObject(), this), this);
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }
}
