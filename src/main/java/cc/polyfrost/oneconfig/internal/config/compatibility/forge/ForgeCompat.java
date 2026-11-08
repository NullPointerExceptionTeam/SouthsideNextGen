package cc.polyfrost.oneconfig.internal.config.compatibility.forge;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.migration.Migrator;
import com.google.gson.JsonObject;
import dev.southside.module.Category;
import java.util.HashMap;
import org.jetbrains.annotations.Nullable;

public class ForgeCompat {
   public static final HashMap<Mod, Runnable> compatMods = new HashMap<>();

   public static class ForgeCompatMod extends Mod {
      private ForgeCompatMod(String name, Category modType, @Nullable String modIcon, @Nullable Migrator migrator) {
         super(name, modType, modIcon, migrator);
         this.config = new Config(this, true, false) {
            @Override
            public void initialize() {
            }

            @Override
            public JsonObject save() {
               return new JsonObject();
            }

            @Override
            public void load(JsonObject base) {
            }

            @Override
            public void openGui() {
               ForgeCompat.compatMods.get(this.mod).run();
            }

            @Override
            public boolean supportsProfiles() {
               return false;
            }
         };
      }

      public ForgeCompatMod(String name, Category modType) {
         this(name, modType, null, null);
      }

      public ForgeCompatMod(String name, Category modType, String icon) {
         this(name, modType, icon, null);
      }
   }
}
