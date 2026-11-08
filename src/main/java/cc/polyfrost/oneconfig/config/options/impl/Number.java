package cc.polyfrost.oneconfig.config.options.impl;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.options.Option;
import org.jetbrains.annotations.NotNull;

public class Number extends Option<java.lang.Number> {
   private final float min;
   private final float max;
   private final int step;
   private final int size;
   private final String description;

   public Number(@NotNull String label, float min, float max, int step, int size, String description, @NotNull java.lang.Number defaultValue) {
      super(label, defaultValue);
      this.min = min;
      this.max = max;
      this.step = step;
      this.size = size;
      this.description = description;
   }

   @Override
   public OptionType type() {
      return OptionType.NUMBER;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   public int getStep() {
      return this.step;
   }

   public int getSize() {
      return this.size;
   }

   public String getDescription() {
      return this.description;
   }
}
