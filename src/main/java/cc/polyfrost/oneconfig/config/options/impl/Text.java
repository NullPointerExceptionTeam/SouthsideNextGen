package cc.polyfrost.oneconfig.config.options.impl;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.options.Option;
import org.jetbrains.annotations.NotNull;

public class Text extends Option<String> {
   private final String placeholder;
   private final boolean secure;
   private final boolean multiline;
   private final String description;
   private final int size;

   public Text(@NotNull String label, String placeholder, boolean secure, boolean multiline, String description, int size, @NotNull String defaultValue) {
      super(label, defaultValue);
      this.placeholder = placeholder;
      this.secure = secure;
      this.multiline = multiline;
      this.description = description;
      this.size = size;
   }

   @Override
   public OptionType type() {
      return OptionType.TEXT;
   }

   public String getPlaceholder() {
      return this.placeholder;
   }

   public boolean isSecure() {
      return this.secure;
   }

   public boolean isMultiline() {
      return this.multiline;
   }

   public String getDescription() {
      return this.description;
   }

   public int getSize() {
      return this.size;
   }
}
