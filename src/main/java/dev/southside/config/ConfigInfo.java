package dev.southside.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.southside.Southside;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import lombok.Generated;

public class ConfigInfo {
   public final String name;
   public final File path;

   public void apply() {
      try {
         if (!this.path.exists() && !this.path.isFile()) {
            return;
         }

         JsonElement element = JsonParser.parseReader(new BufferedReader(new FileReader(this.path)));
         if (element instanceof JsonNull) {
            return;
         }

         JsonObject obj = element.getAsJsonObject();
         Southside.INSTANCE.getModuleManager().modules.forEach(module -> module.loadConfigFromJson(obj));
         Southside.INSTANCE.getModuleManager().saveConfig();
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   @Generated
   public ConfigInfo(String name, File path) {
      this.name = name;
      this.path = path;
   }
}
