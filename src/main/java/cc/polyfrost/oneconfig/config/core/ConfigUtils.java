package cc.polyfrost.oneconfig.config.core;

import cc.polyfrost.oneconfig.config.core.exceptions.InvalidTypeException;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionCategory;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;
import cc.polyfrost.oneconfig.config.options.Option;
import cc.polyfrost.oneconfig.config.options.impl.Button;
import cc.polyfrost.oneconfig.config.options.impl.Checkbox;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.DualOption;
import cc.polyfrost.oneconfig.config.options.impl.Header;
import cc.polyfrost.oneconfig.config.options.impl.Info;
import cc.polyfrost.oneconfig.config.options.impl.KeyBind;
import cc.polyfrost.oneconfig.config.options.impl.Number;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigButton;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigCheckbox;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigColorElement;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigDropdown;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigDualOption;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigHeader;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigInfo;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigKeyBind;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigNumber;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigSlider;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigSwitch;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigTextBox;
import cc.polyfrost.oneconfig.internal.config.profiles.Profiles;
import com.google.gson.FieldAttributes;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigUtils {
   public static BasicOption getOption2(Option option) {
      return getOption2(option, option.category(), option.subcategory());
   }

   public static BasicOption getOption2(Option option, String category, String subcategory) {
      Field field = null;

      try {
         field = Option.class.getDeclaredField("value");
      } catch (NoSuchFieldException var6) {
         throw new RuntimeException(var6);
      }

      field.setAccessible(true);
      switch (option.type()) {
         case SWITCH: {
            Switch casted = (Switch)option;
            return new ConfigSwitch(field, option, casted.getLabel(), casted.getDescription(), category, subcategory, casted.getSize());
         }
         case CHECKBOX: {
            Checkbox casted = (Checkbox)option;
            return new ConfigCheckbox(field, option, casted.getLabel(), casted.getDescription(), category, subcategory, casted.getSize());
         }
         case INFO: {
            Info casted = (Info)option;
            return new ConfigInfo(field, option, casted.getDefaultValue(), category, subcategory, casted.getSize(), casted.getType());
         }
         case HEADER: {
            Header casted = (Header)option;
            return new ConfigHeader(field, option, casted.getValue(), category, subcategory, casted.getSize());
         }
         case COLOR: {
            Color casted = (Color)option;
            return new ConfigColorElement(
               field, option, casted.getLabel(), casted.getDescription(), category, subcategory, casted.getSize(), casted.isAllowAlpha()
            );
         }
         case DROPDOWN: {
            Dropdown casted = (Dropdown)option;
            return new ConfigDropdown(field, option, casted.getLabel(), casted.getDescription(), category, subcategory, casted.getSize(), casted.getOptions());
         }
         case TEXT: {
            Text casted = (Text)option;
            return new ConfigTextBox(
               field,
               option,
               casted.getLabel(),
               casted.getDescription(),
               category,
               subcategory,
               !casted.isSecure() && !casted.isMultiline() ? casted.getSize() : 2,
               casted.getPlaceholder(),
               casted.isSecure(),
               casted.isMultiline()
            );
         }
         case BUTTON: {
            Button casted = (Button)option;
            return new ConfigButton(
               casted.getValue(), option, casted.getLabel(), casted.getDescription(), category, subcategory, casted.getSize(), casted.getText()
            );
         }
         case SLIDER: {
            Slider casted = (Slider)option;
            return new ConfigSlider(
               field,
               option,
               casted.getLabel(),
               casted.getDescription(),
               category,
               subcategory,
               casted.getMin(),
               casted.getMax(),
               casted.getStep(),
               casted.isInstant(),
               casted.getIncrement()
            );
         }
         case NUMBER: {
            Number casted = (Number)option;
            return new ConfigNumber(
               field,
               option,
               casted.getLabel(),
               casted.getDescription(),
               category,
               subcategory,
               casted.getMin(),
               casted.getMax(),
               casted.getStep(),
               casted.getSize()
            );
         }
         case KEYBIND: {
            KeyBind casted = (KeyBind)option;
            return new ConfigKeyBind(field, option, casted.getLabel(), casted.getDescription(), category, subcategory, casted.getSize());
         }
         case DUAL_OPTION: {
            DualOption casted = (DualOption)option;
            return new ConfigDualOption(
               field, option, casted.getDescription(), casted.getDescription(), category, subcategory, casted.getSize(), casted.getLeft(), casted.getRight()
            );
         }
         default:
            return null;
      }
   }

   public static void check(String type, Field field, Class<?>... expectedType) {
      for (Class<?> clazz : expectedType) {
         if (clazz.isAssignableFrom(field.getType())) {
            return;
         }
      }

      throw new InvalidTypeException(
         "Field "
            + field.getName()
            + " in config "
            + field.getDeclaringClass().getName()
            + " is annotated as a "
            + type
            + ", but is not of valid type, expected "
            + Arrays.toString((Object[])expectedType)
            + " (found "
            + field.getType()
            + ")"
      );
   }

   public static ArrayList<BasicOption> getClassOptions(Object object) {
      ArrayList<BasicOption> options = new ArrayList<>();

      for (Field field : getClassFields(object.getClass())) {
         boolean staticFlag = Modifier.isStatic(field.getModifiers());
         if (field.canAccess(staticFlag ? null : object)) {
            try {
               if (field.get(staticFlag ? null : object) instanceof Option<?> option) {
                  options.add(getOption2(option));
               }
            } catch (IllegalAccessException var8) {
            }
         }
      }

      return options;
   }

   public static ArrayList<Field> getClassFields(Class<?> object) {
      ArrayList<Field> fields = new ArrayList<>(Arrays.asList(object.getDeclaredFields()));
      Class<?> parentClass = object;
      Class<?> clazz = object;

      while (true) {
         clazz = clazz.getSuperclass();
         if (clazz == null || clazz == parentClass) {
            return fields;
         }

         fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
         parentClass = clazz;
      }
   }

   public static BasicOption addOptionToPage2(OptionPage page, Option option) {
      BasicOption configOption = getOption2(option);
      if (configOption == null) {
         return null;
      } else {
         getSubCategory(page, configOption.category, configOption.subcategory).options.add(configOption);
         return configOption;
      }
   }

   public static OptionSubcategory getSubCategory(OptionPage page, String categoryName, String subcategoryName) {
      if (!page.categories.containsKey(categoryName)) {
         page.categories.put(categoryName, new OptionCategory());
      }

      OptionCategory category = page.categories.get(categoryName);
      OptionSubcategory subcategory = category.subcategories.stream().filter(s -> s.getName().equals(subcategoryName)).findFirst().orElse(null);
      if (category.subcategories.size() == 0 || subcategory == null) {
         category.subcategories.add(subcategory = new OptionSubcategory(subcategoryName, categoryName));
      }

      return subcategory;
   }

   public static <T extends Annotation> T findAnnotation(Field field, Class<T> annotationType) {
      if (field.isAnnotationPresent(annotationType)) {
         return field.getAnnotation(annotationType);
      } else {
         for (Annotation ann : field.getDeclaredAnnotations()) {
            if (ann.annotationType().isAnnotationPresent(annotationType)) {
               return ann.annotationType().getAnnotation(annotationType);
            }
         }

         return null;
      }
   }

   public static <T extends Annotation> T findAnnotation(Method method, Class<T> annotationType) {
      if (method.isAnnotationPresent(annotationType)) {
         return method.getAnnotation(annotationType);
      } else {
         for (Annotation ann : method.getDeclaredAnnotations()) {
            if (ann.annotationType().isAnnotationPresent(annotationType)) {
               return ann.annotationType().getAnnotation(annotationType);
            }
         }

         return null;
      }
   }

   public static <T extends Annotation> T findAnnotation(FieldAttributes field, Class<T> annotationType) {
      T annotation = (T)field.getAnnotation(annotationType);
      if (annotation != null) {
         return annotation;
      } else {
         for (Annotation ann : field.getAnnotations()) {
            if (ann.annotationType().isAnnotationPresent(annotationType)) {
               return ann.annotationType().getAnnotation(annotationType);
            }
         }

         return null;
      }
   }

   public static <T extends Annotation> T findAnnotation(Class<?> clazz, Class<T> annotationType) {
      if (clazz.isAnnotationPresent(annotationType)) {
         return clazz.getAnnotation(annotationType);
      } else {
         for (Annotation ann : clazz.getDeclaredAnnotations()) {
            if (ann.annotationType().isAnnotationPresent(annotationType)) {
               return ann.annotationType().getAnnotation(annotationType);
            }
         }

         return null;
      }
   }

   public static Object getField(Field field, Object parent) {
      try {
         field.setAccessible(true);
         return field.get(parent);
      } catch (Exception var3) {
         return null;
      }
   }

   public static void setField(Field field, Object value, Object parent) {
      try {
         field.setAccessible(true);
         field.set(parent, value);
      } catch (Exception var4) {
      }
   }

   public static String getCurrentProfile() {
      return Profiles.getCurrentProfile();
   }

   public static File getProfileDir() {
      return Profiles.getProfileDir();
   }

   public static File getProfileFile(String file) {
      return Profiles.getProfileFile(file);
   }
}
