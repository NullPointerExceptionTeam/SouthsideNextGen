package cc.polyfrost.oneconfig.config.migration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonMigrator implements Migrator {
   protected JsonObject object;
   protected HashMap<String, Object> values = null;

   public JsonMigrator(String filePath) {
      File file = new File(filePath);

      try {
         this.object = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
      } catch (Exception var4) {
         var4.printStackTrace();
         this.object = null;
      }
   }

   @Override
   public Object getValue(Field field, @Nullable String name, @Nullable String category, @Nullable String subcategory) {
      if (this.object == null) {
         return null;
      } else {
         if (this.values == null) {
            this.generateValues();
         }

         String key;
         if (field.isAnnotationPresent(JsonName.class)) {
            JsonName annotation = field.getAnnotation(JsonName.class);
            key = annotation.value();
         } else {
            key = field.getName();
         }

         return this.values.get(this.parse(key));
      }
   }

   protected String parse(@NotNull String value) {
      if (value.startsWith("/") || value.startsWith(".")) {
         value = value.substring(1);
      }

      return value.replaceAll("/", ".");
   }

   protected void generateValues() {
      if (this.object != null) {
         this.values = new HashMap<>();

         for (Entry<String, JsonElement> master : this.object.entrySet()) {
            this.loopThroughChildren(master.getKey(), master.getValue().getAsJsonObject());
         }
      }
   }

   protected void loopThroughChildren(String path, JsonObject in) {
      for (Entry<String, JsonElement> element : in.entrySet()) {
         String thisPath = path + "." + element.getKey();
         if (element.getValue().isJsonObject()) {
            this.loopThroughChildren(thisPath, element.getValue().getAsJsonObject());
         } else {
            this.put(thisPath, element.getValue());
         }
      }
   }

   protected void put(String key, JsonElement val) {
      if (val.isJsonNull()) {
         this.values.put(key, null);
      } else if (val.isJsonPrimitive()) {
         this.values.put(key, this.cast(val.getAsJsonPrimitive()));
      } else if (val.isJsonArray()) {
         JsonArray array = val.getAsJsonArray();
         Iterator<JsonElement> iterator = array.iterator();
         Object[] objects = new Object[array.size()];
         int i = 0;

         while (iterator.hasNext()) {
            objects[i] = this.cast(iterator.next().getAsJsonPrimitive());
         }

         this.values.put(key, objects);
      } else {
         this.values.put(key, val);
      }
   }

   private Object cast(JsonPrimitive primitive) {
      if (primitive.isJsonNull()) {
         return null;
      } else if (primitive.isBoolean()) {
         return primitive.getAsBoolean();
      } else if (primitive.isNumber()) {
         Number number = primitive.getAsNumber();
         return number.floatValue() % 1.0F != 0.0F ? number.floatValue() : number.intValue();
      } else {
         return primitive.getAsString();
      }
   }
}
