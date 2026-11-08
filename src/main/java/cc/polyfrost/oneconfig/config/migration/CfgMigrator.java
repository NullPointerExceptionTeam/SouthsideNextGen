package cc.polyfrost.oneconfig.config.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfgMigrator implements Migrator {
   final String filePath;
   final boolean fileExists;
   HashMap<String, HashMap<String, Object>> values;
   final Pattern stringOrFloatPattern = Pattern.compile("S:(?<name>\\S+)=(?<value>\\S+)");
   final Pattern intPattern = Pattern.compile("I:(?<name>\\S+)=(?<value>\\S+)");
   final Pattern doublePattern = Pattern.compile("D:(?<name>\\S+)=(?<value>\\S+)");
   final Pattern booleanPattern = Pattern.compile("B:(?<name>\\S+)=(?<value>\\S+)");
   final Pattern listPattern = Pattern.compile("S:(?<name>\\S+)\\s<");
   final Pattern categoryPattern = Pattern.compile("(?<name>\\S+)\\s\\Q{");

   public CfgMigrator(String filePath) {
      this.filePath = filePath;
      this.fileExists = new File(filePath).exists();
   }

   @Nullable
   @Override
   public Object getValue(Field field, @NotNull String name, @Nullable String category, @Nullable String subcategory) {
      if (!this.fileExists) {
         return null;
      } else {
         if (this.values == null) {
            this.generateValues();
         }

         if (field.isAnnotationPresent(CfgName.class)) {
            CfgName annotation = field.getAnnotation(CfgName.class);
            name = annotation.name();
            category = annotation.category();
         }

         name = this.parse(name);
         if (category == null) {
            if (subcategory != null) {
               category = this.parse(subcategory);
            }
         } else {
            category = this.parse(category);
         }

         return this.values.getOrDefault(category, new HashMap<>()).getOrDefault(name, null);
      }
   }

   protected void generateValues() {
      if (this.values == null) {
         this.values = new HashMap<>();
      }

      try (BufferedReader reader = new BufferedReader(new FileReader(this.filePath))) {
         String currentCategory = null;

         String line;
         while ((line = reader.readLine()) != null) {
            Matcher categoryMatcher = this.categoryPattern.matcher(line);
            if (categoryMatcher.find()) {
               currentCategory = categoryMatcher.group("name");
               if (!this.values.containsKey(currentCategory)) {
                  this.values.put(currentCategory, new HashMap<>());
               }
            }

            if (currentCategory != null) {
               if (line.contains("\"")) {
                  line = line.replaceAll("\"", "").replaceAll(" ", "");
               }

               Matcher booleanMatcher = this.booleanPattern.matcher(line);
               if (booleanMatcher.find()) {
                  this.values.get(currentCategory).put(booleanMatcher.group("name"), Boolean.parseBoolean(booleanMatcher.group("value")));
               } else {
                  Matcher intMatcher = this.intPattern.matcher(line);
                  if (intMatcher.find()) {
                     this.values.get(currentCategory).put(intMatcher.group("name"), Integer.parseInt(intMatcher.group("value")));
                  } else {
                     Matcher doubleMatcher = this.doublePattern.matcher(line);
                     if (doubleMatcher.find()) {
                        this.values.get(currentCategory).put(doubleMatcher.group("name"), Double.parseDouble(doubleMatcher.group("value")));
                     } else {
                        Matcher stringOrFloatMatcher = this.stringOrFloatPattern.matcher(line.trim());
                        if (stringOrFloatMatcher.matches()) {
                           if (line.contains(".")) {
                              try {
                                 this.values
                                    .get(currentCategory)
                                    .put(stringOrFloatMatcher.group("name"), Float.parseFloat(stringOrFloatMatcher.group("value")));
                              } catch (Exception var14) {
                                 this.values.get(currentCategory).put(stringOrFloatMatcher.group("name"), stringOrFloatMatcher.group("value"));
                              }
                           }

                           this.values.get(currentCategory).put(stringOrFloatMatcher.group("name"), stringOrFloatMatcher.group("value"));
                        } else {
                           Matcher listMatcher = this.listPattern.matcher(line.trim());
                           if (listMatcher.matches()) {
                              String name = listMatcher.group("name");
                              ArrayList<String> list = new ArrayList<>();

                              while ((line = reader.readLine()) != null && !line.contains(">")) {
                                 list.add(line.trim());
                              }

                              String[] array = new String[list.size()];
                              list.toArray(array);
                              this.values.get(currentCategory).put(name, array);
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }
   }

   @NotNull
   protected String parse(@NotNull String value) {
      return value.contains("\"") ? value.replaceAll("\"", "").replaceAll(" ", "") : value;
   }
}
