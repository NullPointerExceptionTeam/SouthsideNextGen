package cc.polyfrost.oneconfig.internal;

import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.events.event.ShutdownEvent;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.internal.config.compatibility.forge.ForgeCompat;
import cc.polyfrost.oneconfig.internal.config.core.ConfigCore;
import cc.polyfrost.oneconfig.internal.config.core.KeyBindHandler;
import cc.polyfrost.oneconfig.internal.gui.BlurHandler;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.utils.Notifications;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import dev.southside.orbit.EventHandler;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OneConfig {
   public static final OneConfig INSTANCE = new OneConfig();
   public static final Logger LOGGER = LogManager.getLogger("OneConfig");
   private static boolean initialized = false;

   public OneConfig() {
      EventManager.INSTANCE.register(this);
   }

   public void init() {
      if (!initialized) {
         if (OneConfigConfig.getInstance() == null) {
            OneConfigConfig.getInstance();
         }

         if (Preferences.getInstance() == null) {
            Preferences.getInstance();
         }

         for (Entry<Mod, Runnable> entry : ForgeCompat.compatMods.entrySet()) {
            ConfigCore.mods.add(entry.getKey());
         }

         Notifications.INSTANCE.getClass();
         EventManager.INSTANCE.register(Notifications.INSTANCE);
         GuiUtils.getDeltaTime();

         try {
            EventManager.INSTANCE.register(BlurHandler.INSTANCE);
         } catch (Exception var3) {
            var3.printStackTrace();
         }

         EventManager.INSTANCE.register(new HudCore());
         EventManager.INSTANCE.register(KeyBindHandler.INSTANCE);
         ConfigCore.sortMods();
         initialized = true;
      }
   }

   @EventHandler
   private void onShutdown(ShutdownEvent event) {
      ConfigCore.saveAll();
   }
}
