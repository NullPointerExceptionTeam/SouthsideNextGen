package dev.southside.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.southside.module.setting.Setting;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Generated;

public class ModeSetting extends Setting<String> {
   private final String[] strings;
   public boolean shouldShow;

   public ModeSetting(String name, String value, String... values) {
      super(name);
      this.strings = values;
      this.setValue(value);
   }

   public ArrayList<String> getAsArray() {
      return new ArrayList<>(Arrays.asList(this.strings));
   }

   public boolean isValid(String name) {
      for (String val : this.strings) {
         if (val.equalsIgnoreCase(name)) {
            return true;
         }
      }

      return false;
   }

   public boolean isMode(String value) {
      return this.getValue().equalsIgnoreCase(value);
   }

   public void setValue(String value) {
      for (String val : this.strings) {
         if (val.equalsIgnoreCase(value)) {
            super.setValue(val);
         }
      }
   }

   public JsonPrimitive toJson() {
      return new JsonPrimitive(this.getValue());
   }

   @Override
   public void fromJson(JsonElement element) {
      if (element.isJsonPrimitive()) {
         this.changeValue(element.getAsString());
      }
   }

   public void changeValue(String value) {
      for (String element : this.strings) {
         if (element.equalsIgnoreCase(value)) {
            this.setValue(element);
            break;
         }
      }
   }

   public String getNext() {
      int index = this.getAsArray().indexOf(this.getValue());
      if (index + 1 >= this.strings.length) {
         index -= this.strings.length;
      }

      return this.strings[index + 1];
   }

   @Generated
   public String[] getStrings() {
      return this.strings;
   }
}
