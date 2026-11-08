package cc.polyfrost.oneconfig.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.function.Consumer;

public final class JsonUtils {
   public static final JsonParser PARSER = new JsonParser();

   public static boolean ifValid(String json, Consumer<JsonElement> consumer) {
      try {
         consumer.accept(PARSER.parse(json));
         return true;
      } catch (Exception var3) {
         return false;
      }
   }

   public static boolean isValid(String json) {
      try {
         PARSER.parse(json);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   public static JsonElement parseString(String string, boolean catchExceptions) {
      try {
         return PARSER.parse(string);
      } catch (Exception var3) {
         if (catchExceptions) {
            return null;
         } else {
            throw var3;
         }
      }
   }

   public static JsonElement parseString(String string) {
      return parseString(string, true);
   }
}
