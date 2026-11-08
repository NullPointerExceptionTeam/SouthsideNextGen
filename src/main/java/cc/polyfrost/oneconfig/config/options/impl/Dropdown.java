package cc.polyfrost.oneconfig.config.options.impl;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.options.Option;
import java.util.Arrays;
import net.minecraft.class_3532;
import org.jetbrains.annotations.NotNull;

public class Dropdown extends Option<Integer> {
   private final String[] options;
   private final String description;
   private final int size;

   public Dropdown(@NotNull String label, String[] options, String description, int size, @NotNull Integer defaultValue) {
      super(label, defaultValue);
      this.options = options;
      this.description = description;
      this.size = size;
   }

   public Dropdown(String name, String value, String... values) {
      super(name, Arrays.asList(values).indexOf(value));
      this.options = values;
      this.description = "";
      this.size = 1;
   }

   public void setValue(Integer value) {
      if (this.options != null) {
         super.setValue(class_3532.method_15340(value, 0, this.options.length - 1));
      }
   }

   public String getMode() {
      return this.options[this.getValue()];
   }

   @Override
   public OptionType type() {
      return OptionType.DROPDOWN;
   }

   public String[] getOptions() {
      return this.options;
   }

   public String getDescription() {
      return this.description;
   }

   public int getSize() {
      return this.size;
   }

   public boolean isMode(String classic) {
      return this.options[this.getValue()].equalsIgnoreCase(classic);
   }
}
