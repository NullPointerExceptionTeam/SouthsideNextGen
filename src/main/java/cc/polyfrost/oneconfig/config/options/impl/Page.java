package cc.polyfrost.oneconfig.config.options.impl;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import cc.polyfrost.oneconfig.config.options.Option;
import org.jetbrains.annotations.NotNull;

public class Page extends Option<Object> {
   private final PageLocation location;
   private final String description;

   public Page(@NotNull String label, PageLocation location, String description, @NotNull Object defaultValue) {
      super(label, defaultValue);
      this.location = location;
      this.description = description;
   }

   @Override
   public OptionType type() {
      return null;
   }

   public PageLocation getLocation() {
      return this.location;
   }

   public String getDescription() {
      return this.description;
   }
}
