package cc.polyfrost.oneconfig.internal.config;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.WrappedValue;
import java.util.ArrayList;
import java.util.List;

public class OneConfigConfig extends InternalConfig {
   public static WrappedValue<String> currentProfile = new WrappedValue<>("Current Profile", "default");
   public static boolean autoUpdate = false;
   public static int updateChannel = 0;
   public static List<String> favoriteMods = new ArrayList<>();
   public static List<OneColor> favoriteColors = new ArrayList<>(6);
   public static List<OneColor> recentColors = new ArrayList<>(6);
   public static boolean australia = false;
   private static OneConfigConfig INSTANCE;

   public OneConfigConfig() {
      super("", "Southside_OneConfig.json");
      this.initialize();
      INSTANCE = this;
   }

   public static OneConfigConfig getInstance() {
      return INSTANCE == null ? (INSTANCE = new OneConfigConfig()) : INSTANCE;
   }
}
