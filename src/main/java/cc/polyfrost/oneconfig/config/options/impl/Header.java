package cc.polyfrost.oneconfig.config.options.impl;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.options.Option;
import org.jetbrains.annotations.NotNull;

public class Header extends Option<String> {
   private final int size;

   public Header(@NotNull String label, int size, @NotNull String defaultValue) {
      super(label, defaultValue);
      this.size = size;
   }

   @Override
   public OptionType type() {
      return OptionType.HEADER;
   }

   public int getSize() {
      return this.size;
   }
}
