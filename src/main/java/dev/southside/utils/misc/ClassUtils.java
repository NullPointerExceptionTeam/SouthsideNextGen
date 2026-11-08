package dev.southside.utils.misc;

import dev.southside.module.setting.Setting;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
   public static List<Setting<?>> getValues(Class<?> clazz, Object instance) {
      List<Setting<?>> values = new ArrayList<>();
      Field[] fields = clazz.getDeclaredFields();

      for (Field valueField : fields) {
         valueField.setAccessible(true);

         try {
            Object value = valueField.get(instance);
            if (value instanceof Setting) {
               values.add((Setting<?>)value);
            }
         } catch (IllegalAccessException var9) {
            var9.printStackTrace();
         }
      }

      return values;
   }
}
