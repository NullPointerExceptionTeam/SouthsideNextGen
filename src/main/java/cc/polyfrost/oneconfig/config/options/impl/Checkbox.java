package cc.polyfrost.oneconfig.config.options.impl;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.options.Option;
import org.jetbrains.annotations.NotNull;

public class Checkbox extends Option<Boolean> {
   private final String description;
   private final int size;

   public Checkbox(@NotNull String label, @NotNull Boolean defaultValue, String description, int size) {
      super(label, defaultValue);
      this.description = description;
      this.size = size;
   }

   @Override
   public OptionType type() {
      return OptionType.CHECKBOX;
   }

   public String getDescription() {
      return this.description;
   }

   public int getSize() {
      return this.size;
   }
}
