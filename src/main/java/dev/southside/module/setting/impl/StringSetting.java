package dev.southside.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.southside.module.setting.Setting;

public class StringSetting extends Setting<String> {
   public String text;

   protected StringSetting(String name, String defaultText) {
      super(name);
      this.text = defaultText;
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.getValue());
   }

   @Override
   public void fromJson(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.setValue(element.getAsString());
      }
   }
}
