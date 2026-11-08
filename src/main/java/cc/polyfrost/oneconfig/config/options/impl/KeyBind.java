package cc.polyfrost.oneconfig.config.options.impl;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.options.Option;
import org.jetbrains.annotations.NotNull;

public class KeyBind extends Option<OneKeyBind> {
   private final String description;
   private final int size;

   public KeyBind(@NotNull String label, String description, int size, @NotNull OneKeyBind defaultValue) {
      super(label, defaultValue);
      this.description = description;
      this.size = size;
   }

   @Override
   public OptionType type() {
      return OptionType.KEYBIND;
   }

   public String getDescription() {
      return this.description;
   }

   public int getSize() {
      return this.size;
   }
}
