package dev.southside.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.southside.module.setting.Setting;

public class BooleanSetting extends Setting<Boolean> {
   public BooleanSetting(String name, boolean enabled) {
      super(name);
      this.setValue(enabled);
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.getValue());
   }

   @Override
   public void fromJson(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.setValue(element.getAsBoolean() || element.getAsString().equals("true"));
      }
   }
}
