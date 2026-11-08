package dev.southside.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.southside.module.setting.Setting;
import java.awt.Color;

public class ColorSetting extends Setting<Color> {
   public Color color;

   public ColorSetting(String name, Color defaultColor) {
      super(name);
      this.color = defaultColor;
      this.setValue(this.color);
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.color.getRGB());
   }

   @Override
   public void fromJson(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.color = new Color(element.getAsInt());
      }
   }
}
