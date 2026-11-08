package cc.polyfrost.oneconfig.internal.config.core;

import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConfigCore {
   public static List<Mod> mods = new ArrayList<>();

   public static void saveAll() {
      for (Mod modData : mods) {
         modData.config.save();
      }
   }

   public static void reInitAll() {
      for (Mod modData : mods) {
         if (modData.config.supportsProfiles()) {
            modData.config.reInitialize();
         }
      }

      HudCore.reInitHuds();
      KeyBindHandler.INSTANCE.reInitKeyBinds();
   }

   public static void reset() {
      for (Mod modData : mods) {
         if (modData.config.supportsProfiles()) {
            modData.config.reset();
         }
      }

      HudCore.reInitHuds();
      KeyBindHandler.INSTANCE.reInitKeyBinds();
   }

   public static void sortMods() {
      List<Mod> mods = new ArrayList<>(ConfigCore.mods);
      ConfigCore.mods = mods.stream().filter(mod -> OneConfigConfig.favoriteMods.contains(mod.name)).sorted().collect(Collectors.toList());
      mods.removeAll(ConfigCore.mods);
      ConfigCore.mods.addAll(mods.stream().sorted().collect(Collectors.toList()));
      OneConfigConfig.getInstance().save();
   }

   static {
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ConfigCore::saveAll, 0L, 30L, TimeUnit.SECONDS);
   }
}
