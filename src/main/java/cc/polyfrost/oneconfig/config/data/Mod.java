package cc.polyfrost.oneconfig.config.data;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.migration.Migrator;
import dev.southside.module.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Mod implements Comparable<Mod> {
   @NotNull
   public final String name;
   public final Category modType;
   @Nullable
   public final String modIcon;
   public final int iconWidth;
   public final int iconHeight;
   @Nullable
   public final Migrator migrator;
   @NotNull
   public final OptionPage defaultPage;
   public Config config;

   public Mod(@NotNull String name, Category modType, @Nullable String modIcon, int iconWidth, int iconHeight, @Nullable Migrator migrator) {
      this.name = name;
      this.modType = modType;
      this.modIcon = modIcon;
      this.iconWidth = iconWidth;
      this.iconHeight = iconHeight;
      this.migrator = migrator;
      this.defaultPage = new OptionPage(name, this);
   }

   public Mod(String name, Category modType, @Nullable String modIcon, Migrator migrator) {
      this(name, modType, modIcon, -1, -1, null);
   }

   public Mod(String name, Category modType, @Nullable String modIcon, int iconWidth, int iconHeight) {
      this(name, modType, modIcon, iconWidth, iconHeight, null);
   }

   public Mod(String name, Category modType, @Nullable String modIcon) {
      this(name, modType, modIcon, -1, -1, null);
   }

   public Mod(String name, Category modType, @Nullable Migrator migrator) {
      this(name, modType, null, -1, -1, migrator);
   }

   public Mod(String name, Category modType) {
      this(name, modType, null, -1, -1, null);
   }

   public int compareTo(@NotNull Mod mod) {
      return this.name.compareTo(mod.name);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else {
         return obj instanceof Mod mod ? this.name.equals(mod.name) : false;
      }
   }
}
