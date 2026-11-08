package dev.southside.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.southside.module.setting.Setting;

public class NumberSetting extends Setting<Double> {
   private double minimum;
   private double maximum;
   private double increment;

   public NumberSetting(String name, double value, double minimum, double maximum, double increment) {
      super(name);
      this.setValue(value);
      this.minimum = minimum;
      this.maximum = maximum;
      this.increment = increment;
   }

   public NumberSetting(String name, Number value, Number minimum, Number maximum, Number increment) {
      super(name);
      this.setValue(value.doubleValue());
      this.minimum = minimum.doubleValue();
      this.maximum = maximum.doubleValue();
      this.increment = increment.doubleValue();
   }

   public double getMaximum() {
      return this.maximum;
   }

   public void setMaximum(double maximum) {
      this.maximum = maximum;
   }

   public double getMinimum() {
      return this.minimum;
   }

   public void setMinimum(double minimum) {
      this.minimum = minimum;
   }

   public double getIncrement() {
      return this.increment;
   }

   public void setIncrement(double increment) {
      this.increment = increment;
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.getValue());
   }

   @Override
   public void fromJson(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.setValue(element.getAsDouble());
      }
   }
}
