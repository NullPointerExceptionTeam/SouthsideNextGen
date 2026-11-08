package cc.polyfrost.oneconfig.config.migration;

import cc.polyfrost.oneconfig.config.core.OneColor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class VigilanceMigrator implements Migrator {
   private static final Pattern categoryPattern = Pattern.compile("\\[\"?(?<category>[^.\\[\\]\"]+)\"?(\\.\"?(?<subcategory>[^.\\[\\]\"]+)\"?)?]");
   private static final Pattern booleanPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = (?<value>true|false)");
   private static final Pattern numberPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = (?<value>[\\d.]+)");
   private static final Pattern stringPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = \"(?<value>.+)\"");
   private static final Pattern colorPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = \"(?<value>(\\d{1,3},){3}\\d{1,3})\"");
   protected final String filePath;
   protected HashMap<String, HashMap<String, HashMap<String, Object>>> values = null;
   protected final boolean fileExists;

   public VigilanceMigrator(String filePath) {
      this.filePath = filePath;
      this.fileExists = new File(filePath).exists();
   }

   @Override
   public Object getValue(Field field, @NotNull String name, @NotNull String category, @NotNull String subcategory) {
      if (!this.fileExists) {
         return null;
      } else {
         if (this.values == null) {
            this.generateValues();
         }

         if (field.isAnnotationPresent(VigilanceName.class)) {
            VigilanceName annotation = field.getAnnotation(VigilanceName.class);
            name = annotation.name();
            category = annotation.category();
            subcategory = annotation.subcategory();
         }

         name = this.parse(name);
         category = this.parse(category);
         subcategory = this.parse(subcategory);
         return this.values.getOrDefault(category, new HashMap<>()).getOrDefault(subcategory, new HashMap<>()).getOrDefault(name, null);
      }
   }

   @NotNull
   protected String parse(@NotNull String value) {
      return value.toLowerCase().replace(" ", "_");
   }

   protected void generateValues() {
      if (this.values == null) {
         this.values = new HashMap<>();
      }

      try (BufferedReader reader = new BufferedReader(new FileReader(this.filePath))) {
         String currentCategory = null;
         String currentSubcategory = null;

         String line;
         while ((line = reader.readLine()) != null) {
            Matcher categoryMatcher = categoryPattern.matcher(line);
            if (categoryMatcher.find()) {
               currentCategory = categoryMatcher.group("category");
               currentSubcategory = categoryMatcher.group("subcategory");
               if (!this.values.containsKey(currentCategory)) {
                  this.values.put(currentCategory, new HashMap<>());
               }

               if (!this.values.get(currentCategory).containsKey(currentSubcategory)) {
                  this.values.get(currentCategory).put(currentSubcategory, new HashMap<>());
               }
            } else if (currentCategory != null) {
               HashMap<String, Object> options = this.values.get(currentCategory).get(currentSubcategory);
               Matcher booleanMatcher = booleanPattern.matcher(line);
               if (booleanMatcher.find()) {
                  options.put(booleanMatcher.group("name"), Boolean.parseBoolean(booleanMatcher.group("value")));
               } else {
                  Matcher numberMatcher = numberPattern.matcher(line);
                  if (numberMatcher.find()) {
                     String value = numberMatcher.group("value");
                     if (value.contains(".")) {
                        options.put(numberMatcher.group("name"), Float.parseFloat(value));
                     } else {
                        options.put(numberMatcher.group("name"), Integer.parseInt(value));
                     }
                  } else {
                     Matcher colorMatcher = colorPattern.matcher(line);
                     if (colorMatcher.find()) {
                        String[] strings = colorMatcher.group("value").split(",");
                        int[] values = new int[4];

                        for (int i = 0; i < 4; i++) {
                           values[i] = Integer.parseInt(strings[i]);
                        }

                        options.put(colorMatcher.group("name"), new OneColor(values[0], values[1], values[2], values[3]));
                     }

                     Matcher stringMatcher = stringPattern.matcher(line);
                     if (stringMatcher.find()) {
                        options.put(stringMatcher.group("name"), stringMatcher.group("value"));
                     }
                  }
               }
            }
         }
      } catch (Exception var15) {
         var15.printStackTrace();
      }
   }
}
